package it.thecrawlers;

import it.thecrawlers.crawler.CrawlConfigurer;

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
		CrawlConfigurer crawlerConfigurer = BeanFactoryUtils.beanOfType(ctx, CrawlConfigurer.class);
		try {
			crawlerConfigurer.start();
		} catch (Exception e) {
			logger.error("Crawler initializaton failed", e);
		}		
	}

}
