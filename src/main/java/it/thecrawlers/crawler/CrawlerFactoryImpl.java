package it.thecrawlers.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.WebCrawler;

@Component
public class CrawlerFactoryImpl<T extends WebCrawler> implements CrawlerFactory {
	@Autowired
	private T obj;

	@SuppressWarnings("unchecked")
	@Override
	public T createCrawlerInstance() {
		return (T) obj;
	}
}
