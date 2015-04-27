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
	
//	@Autowired
//	private CrawlController crawlController;
	@Autowired
	private ItemReviewsPageParser parser;
	@Autowired
	private ExpandedUserReviewFetcher reviewFetcher;
	@Autowired
	private ItemDAO itemDao;
	@Autowired
	private ReviewDAO reviewDao;
	
	public void processReviewPage(String url, String path, String htmlContent) {
		if (parser.isItemReviewsPage(path) ){
				Item item = parser.parseItem(htmlContent, path, url);
				
				if (itemDao.exists(item.getId())) {
					item = itemDao.findOne(item.getId());
					item.setCrawlDate(new Date());
				}				
				
				Set<Review> reviews = parser.parseReviews(htmlContent, path);
				if (!reviews.isEmpty()) {
					ArrayList<String> revStringList = new ArrayList<String>();
					for (Review review : reviews) {
						revStringList.add(review.getId());
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
							if (!reviewDao.exists(review.getId())) {
								item.getReviews().add(review);
							} else {
								// TODO:update review with new one
							}
						}
					}
				}
				item = itemDao.save(item);
				
//				saveStringToFile(path, htmlContent);				
			}
		}

	private void saveStringToFile(String path, String htmlContent) {
		String fileNameFormPath = path.replaceAll("[^a-zA-Z0-9.-]", "_");				
		try {
			FileUtils.writeStringToFile(new File("d:/temp/crawldata", fileNameFormPath), htmlContent);
		} catch (IOException e) {
			logger.error("Cannot write page to the file", e);;
		}
	}
	
}
