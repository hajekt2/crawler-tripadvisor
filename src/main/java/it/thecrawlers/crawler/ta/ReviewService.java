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

	public void processReviewPage(String url, String path, String htmlContent) {
		if (parser.isItemReviewsPage(path)) {
			//parse item
			Item item = parser.parseItem(htmlContent, path, url);
			
			//if item exists in db then use version stored in db
			if (itemDao.exists(item.getId())) {
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
			} else {			
				//no reviews exists for thir item so fetch all
				for (Review review : reviews) {
					revStringList.add(review.getId());
				}
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

			if (item.getReviews().isEmpty()) {
				item.getReviews().addAll(reviews);
			} else {
				for (Review review : reviews) {
					//it should always be true that review does not exists
					if (!reviewDao.exists(review.getId())) {
						item.getReviews().add(review);
					}
				}
			}
			item = itemDao.save(item);

		}
	}

	private void saveStringToFile(String path, String htmlContent) {
		String fileNameFormPath = path.replaceAll("[^a-zA-Z0-9.-]", "_");
		try {
			FileUtils.writeStringToFile(new File("d:/temp/crawldata", fileNameFormPath), htmlContent);
		} catch (IOException e) {
			logger.error("Cannot write page to the file", e);
			;
		}
	}

}
