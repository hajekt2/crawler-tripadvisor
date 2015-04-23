package it.thecrawlers.crawler;

import edu.uci.ics.crawler4j.crawler.Page;

public interface CrawlHandler {
	public void processPage(Page page);
}
