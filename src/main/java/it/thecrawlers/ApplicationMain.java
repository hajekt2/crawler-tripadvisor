package it.thecrawlers;

import it.thecrawlers.crawler.ExtendableCrawlerController;
import it.thecrawlers.crawler.ta.ReviewService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationMain {

	static final Logger logger = LoggerFactory.getLogger(ApplicationMain.class);

	public static void main(String[] args) {

		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		ExtendableCrawlerController controller = BeanFactoryUtils.beanOfType(ctx, ExtendableCrawlerController.class);
		ReviewService reviewService = BeanFactoryUtils.beanOfType(ctx, ReviewService.class);
		try {
			controller.start("http://no.tripadvisor.com/Hotels-g1006204-Trysil_Municipality_Hedmark_Eastern_Norway-Hotels.html");
			reviewService.processReviewsForAllItems();
		} catch (Exception e) {
			logger.error("Crawler initializaton failed", e);
		}		
	}

}
