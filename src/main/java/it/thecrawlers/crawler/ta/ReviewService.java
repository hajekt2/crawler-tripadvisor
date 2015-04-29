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

@Component
public class ReviewService {

	static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

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
		if (reviews.isEmpty())
			return;

		//create a list of full text reviews for ids that should be fetched
		ArrayList<String> revStringList = new ArrayList<String>();
		if (!item.getReviews().isEmpty()) {
			//fetch only reviews that are not in db
			for (Review review : reviews) {
				if (!item.getReviews().contains(review))
					revStringList.add(review.getId());
			}				
			logger.debug("Fetching full reviews for non-existing items[{}]: {}", revStringList.size(), revStringList.toString());
		} else {
			//no reviews exists for this item so fetch all
			for (Review review : reviews) {
				revStringList.add(review.getId());
			}
			logger.debug("Fetching full reviews for all[{}]: {}", revStringList.size(), revStringList.toString());
		}
			
		String expandedUserReviewHtml = null;
		try {
			expandedUserReviewHtml = reviewFetcher.getExpandedUserReview(item.getId(), item.getLocation().getId(),
					revStringList);
		} catch (IOException e) {
			logger.error("Failed to retrieve ExpandedUserReview", e);
		}
		if (expandedUserReviewHtml != null)
			parser.parseExpandedUserReview(expandedUserReviewHtml, reviews);

		item.getReviews().addAll(reviews);
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
//			for( int i = locationMap.size() -1; i >= 0 ; i --){
//			    String locationId = locationMap.get(i);
//				if (!locationDao.exists(locationId)) {
//					logger.trace("Creating new location = {}, {}", locationId, locationMap.get(locationId));
//					parentLocation = new Location(locationId, locationMap.get(locationId), parentLocation);
//				} else {
//					parentLocation = locationDao.findOne(locationId);
//				}
//			}
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

	private void saveStringToFile(File folder, String path, String htmlContent) {
		String fileNameFormPath = path.replaceAll("[^a-zA-Z0-9.-]", "_");
		try {
			FileUtils.writeStringToFile(new File(folder, fileNameFormPath), htmlContent);
		} catch (IOException e) {
			logger.error("Cannot write page to the file", e);
		}
	}

}
