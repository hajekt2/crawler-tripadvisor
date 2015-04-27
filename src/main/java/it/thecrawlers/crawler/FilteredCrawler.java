package it.thecrawlers.crawler;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Adds regex filters (filters/matches) URL functionality to WebCrawler
 */
@Component
public class FilteredCrawler extends WebCrawler {

	static final Logger logger = LoggerFactory.getLogger(FilteredCrawler.class);

	@Autowired
	private CrawlHandler handler;

	@Value("${filters}")
	private String filters;
	@Value("${matchers}")
	private String matchers;
	
	/**
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(Page page, WebURL url) {
		boolean result = false;
		String href = url.getURL().toLowerCase();
		result =  (!Pattern.compile(filters, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(href).matches() &&
				Pattern.compile(matchers, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(href).matches());		
		if (result) logger.trace("shouldVisit {}", href);
		return result;
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		logger.trace("visiting {}", page.getWebURL().getURL());	
		handler.processPage(page);
	}
	
}
