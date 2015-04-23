package it.thecrawlers.crawler;

import edu.uci.ics.crawler4j.crawler.WebCrawler;

public interface CrawlerFactory {
    <T extends WebCrawler> T createCrawlerInstance();
}
