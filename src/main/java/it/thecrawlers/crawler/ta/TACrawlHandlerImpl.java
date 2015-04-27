package it.thecrawlers.crawler.ta;

import it.thecrawlers.crawler.CrawlHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

@Component
public class TACrawlHandlerImpl implements CrawlHandler {

	static final Logger logger = LoggerFactory.getLogger(TACrawlHandlerImpl.class);
	
	@Autowired
	private ReviewService reviewService;
	
	@Override
	public void processPage(Page page) {
		String path = page.getWebURL().getPath();
		String url = page.getWebURL().getURL().toString();
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String htmlContent = htmlParseData.getHtml();
			try {
				reviewService.processReviewPage(url, path, htmlContent);
			} catch (Exception e) {
				logger.error("Processing error: {}", e);
				throw e;
			}
			
		}
	}
	
}
