package it.thecrawlers.crawler.ta;

import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.crawler.dao.LocationDAO;
import it.thecrawlers.crawler.dao.ReviewDAO;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Location;
import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class ReviewService {

	static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

	//limit of how many review items can be queried in single request
	static final int EX_USER_REVIEW_LIMIT = 20;
	
	@Autowired
	private ItemReviewsPageParser parser;
	@Autowired
	private ExpandedUserReviewFetcher reviewFetcher;
	@Autowired
	private ItemDAO itemDao;
	@Autowired
	private ReviewDAO reviewDao;
	@Autowired
	private LocationDAO locationDao;

	@Transactional
	public void processPage(String url, String path, String htmlContent) {
		if (parser.isItemReviewsPage(path)) {
			processReviewPage(url, path, htmlContent);
//			saveStringToFile(new File("d:/temp/crawldata"), path, htmlContent);
		}
	}

	@Transactional
	public void processReviewPage(String url, String path, String htmlContent) {	
		//parse item
		Item item = parser.parseItem(htmlContent, path, url);
			
		//if item exists in db then use version stored in db
		if (itemDao.exists(item.getId())) {
			logger.debug("Item exists in DB, updating item with id = {}", item.getId());
			item = itemDao.findOne(item.getId());
			item.setCrawlDate(new Date());
			//TODO:update also other fields if changed
		} else {
			item.setLocation(processLocations(path, htmlContent));
		}

		//parse review details (but not text since it is not always complete)
		Set<Review> reviews = parser.parseReviews(htmlContent, path);
		if (reviews.isEmpty()) {
			logger.debug("No reviews found for item {}", item.getId());
			return;
		}

		//create a list of full text reviews for ids that should be fetched
		if (!item.getReviews().isEmpty()) {
			//add only reviews that are not in db
			for (Review review : reviews) {
				if (!item.getReviews().contains(review))
					item.getReviews().add(review);
			}				
			logger.debug("Added non existing reviews to DB, total is [{}]", item.getReviews().size());
		} else {
			//no reviews exists for this item so add all
			item.getReviews().addAll(reviews);
			logger.debug("Added all reviews to DB [{}], total is [{}]", reviews.size(), item.getReviews().size());
		}		
		
		item = itemDao.save(item);
	}
	
	public Location processLocations(String path, String htmlContent) {
		//parse location information
		LinkedMap<String, String> locationMap = parser.parseLocations(htmlContent, path);
		logger.trace("Locations found: {}", locationMap.toString());
		//the last element is the closest location so check if it exists in db
		Location parentLocation = null;
		if (locationDao.exists(locationMap.lastKey())) {
			//use the location from DB, closest one
			parentLocation = locationDao.findOne(locationMap.lastKey());
			logger.debug("Location found in DB, id = {}", locationMap.lastKey());
		} else {
			logger.debug("Location does not found in DB, id = {}", locationMap.lastKey());
			//store new location into DB
			//start from the most general location
			for(String locationId : locationMap.keySet()) {
				if (!locationDao.exists(locationId)) {
					logger.trace("Creating new location = {}, {}", locationId, locationMap.get(locationId));
					parentLocation = new Location(locationId, locationMap.get(locationId), parentLocation);
				} else {
					parentLocation = locationDao.findOne(locationId);
				}
			}
			//parentLocation contains the closest location info					
		}
		return parentLocation;
	}

	@Transactional
	public void processReviews(Item item) {
		
		ArrayList<Review> reviewList = new ArrayList<Review>(item.getReviews());
		
		int steps = item.getReviews().size() / EX_USER_REVIEW_LIMIT;
		if (item.getReviews().size() % EX_USER_REVIEW_LIMIT != 0) steps++;
		int max;
		
		for (int i = 0; i < steps; i++) {
	        //build string list of review ids
			ArrayList<String> reviewStringList = new ArrayList<String>();
			logger.debug("Processing reviews in step {} of {}", i+1, steps);
			
			if (item.getReviews().size() < (i+1)*EX_USER_REVIEW_LIMIT) max = item.getReviews().size();
			else max = (i+1)*EX_USER_REVIEW_LIMIT;
			for (int j = (i*EX_USER_REVIEW_LIMIT); j < max; j++) {
	            reviewStringList.add(reviewList.get(j).getId());				
	        }
			
			String expandedUserReviewHtml = null;
			try {
				expandedUserReviewHtml = reviewFetcher.getExpandedUserReview(item.getId(), item.getLocation().getId(),
						reviewStringList);
			} catch (IOException e) {
				logger.error("Failed to retrieve ExpandedUserReview", e);
			}
			if (expandedUserReviewHtml != null)
				parser.parseExpandedUserReview(expandedUserReviewHtml, item.getReviews());
			
		}

		item = itemDao.save(item);
	}
		
	@Transactional
	public void processReviewsForAllItems() {
		Iterable<Item> allItems = itemDao.findAll();
		int position = 1;
		for (Item item : allItems) {
			logger.debug("Processing item id [{}] for all reviews: {} of {}", item.getId(), position, Iterables.size(allItems));
			processReviews(item);
//			itemDao.save(item);
			position++;
		}
	}
	
	private void saveStringToFile(File folder, String path, String htmlContent) {
		String fileNameFormPath = path.replaceAll("[^a-zA-Z0-9.-]", "_");
		try {
			FileUtils.writeStringToFile(new File(folder, fileNameFormPath), htmlContent);
		} catch (IOException e) {
			logger.error("Cannot write page to the file", e);
		}
	}

}
