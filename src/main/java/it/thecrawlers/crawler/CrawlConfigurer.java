package it.thecrawlers.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Spring configurable CrawlConfig
 */
@Component
public class CrawlConfigurer {

	static final Logger logger = LoggerFactory.getLogger(FilteredCrawler.class);

	@Autowired
	CrawlerFactory crawlerFactory;
	
	@Value("${seed}")	
	private String seed;
	@Value("${numberOfCrawlers}")	
	private int numberOfCrawlers;
	@Value("${crawlStorageFolder}")	
	private String crawlStorageFolder;
	@Value("${politenessDelay}")	
	private int politenessDelay;
	@Value("${maxDepthOfCrawling}")	
	private int maxDepthOfCrawling;
	@Value("${maxPagesToFetch}")	
	private int maxPagesToFetch;
	@Value("${resumableCrawling}")	
	private boolean resumableCrawling;	
	
	public void start() throws Exception {

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setPolitenessDelay(politenessDelay);
		config.setMaxDepthOfCrawling(maxDepthOfCrawling);
		config.setMaxPagesToFetch(maxPagesToFetch);
//TODO:proxy
//		config.setProxyHost("proxyserver.example.com");
//		config.setProxyPort(8080);
//		config.setProxyUsername(username);
//		config.getProxyPassword(password);
		config.setResumableCrawling(resumableCrawling);

		//Instantiate the controller for this crawl.
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

		ExtendableCrawlerController controller = new ExtendableCrawlerController(config, pageFetcher, robotstxtServer);
		controller.addSeed(seed);
		controller.start(crawlerFactory, numberOfCrawlers);
	}
}
