package it.thecrawlers.crawler.ta;

import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.crawler.dao.ReviewDAO;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

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

	@Transactional
	public void processReviewPage(String url, String path, String htmlContent) {
		if (parser.isItemReviewsPage(path)) {
			//parse item
			Item item = parser.parseItem(htmlContent, path, url);
			
			//if item exists in db then use version stored in db
			if (itemDao.exists(item.getId())) {
				logger.debug("Item exists in DB, updating item with id = {}", item.getId());
				item = itemDao.findOne(item.getId());
				item.setCrawlDate(new Date());
				//TODO:update also other fields if changed
			}

			//parse review details (but not text sine it is not always complete)
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
				expandedUserReviewHtml = reviewFetcher.getExpandedUserReview(item.getId(), item.getLocationId(),
						revStringList);
			} catch (IOException e) {
				logger.error("Failed to retrieve ExpandedUserReview", e);
			}
			if (expandedUserReviewHtml != null)
				parser.parseExpandedUserReview(expandedUserReviewHtml, reviews);

			item.getReviews().addAll(reviews);
			item = itemDao.save(item);

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
