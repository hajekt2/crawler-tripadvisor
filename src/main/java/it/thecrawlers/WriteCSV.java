package it.thecrawlers;

import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class WriteCSV {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	private static final String START_SEP = "\"";
	private static final String SEP = "\",\"";
	private static final String END_SEP = "\"" + System.getProperty("line.separator");

	public static void main(String[] args) {

		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		ItemDAO itemDao = BeanFactoryUtils.beanOfType(ctx, ItemDAO.class);

		File csvFile = new File("d:\\temp\\review.csv.txt");
		logger.info("Writing DB content into file: {}", csvFile.getAbsolutePath());

		try {
			FileUtils.write(csvFile, START_SEP + 
					"reviewId" + SEP + 
					"author" + SEP + 
					"date" + SEP + 
					"rating" + SEP +
					"title" + SEP + 
					"text" + SEP + 
					"hotelId" + SEP + 
					"hotelName" + SEP + 
					"locationId" + SEP +
					"locationName" + SEP + 
					"fullLocationName" + END_SEP, "ISO-8859-1", false);

			Iterable<Item> items = itemDao.findAll();
			for (Item item : items) {
				for (Review review : item.getReviews()) {
	    			try {
	    				FileUtils.write(csvFile, START_SEP+
	    					review.getId()+SEP+
	    					review.getAuthor().replaceAll("\\r\\n|\\r|\\n", " ")+SEP+
	    					new SimpleDateFormat("yyyy-mm-dd").format(review.getDate())+SEP+
	    					review.getRating()+SEP+
	    					formatTitle(review.getTitle())+SEP+
	    					review.getText().replaceAll("\\r\\n|\\r|\\n", " ")+SEP+
	    					item.getId()+SEP+
	    					item.getName()+SEP+
	    					item.getLocation().getId()+SEP+
	    					item.getLocation().getName()+SEP+
	    					item.getLocation().getFullName()+END_SEP, "ISO-8859-1", true);
	    			} catch (Exception e) {
	    				logger.error("Error reading review id = "+review.getId(), e);
	    			}
	    				
				}

			}
			logger.info("Writing completed");
		} catch (IOException e) {
			logger.error("Processing error", e);
		}

	}

	private static String formatTitle(String title) {
		title = title.replaceAll("\\r\\n|\\r|\\n", " ");
		title = title.substring(1, title.length() - 1);
		return title;
	}
}
