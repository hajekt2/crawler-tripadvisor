/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.thecrawlers.crawler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * This class extends CrawlController, providing functionality for using user
 * supplied Crawler factory, instead of suppling class of Crawler to be
 * automatically instantiated.
 */
@Component
public class ExtendableCrawlerController extends CrawlController {

	static final Logger logger = LoggerFactory.getLogger(ExtendableCrawlerController.class);

	@Autowired
	private CrawlerFactory crawlerFactory;
	@Value("${numberOfCrawlers}")	
	private int numberOfCrawlers;
	
	@Autowired
	public ExtendableCrawlerController(@Qualifier("crawlConfig") CrawlConfig config, 
			@Qualifier("pageFetcher") PageFetcher pageFetcher, 
			@Qualifier("robotstxtServer") RobotstxtServer robotstxtServer) throws Exception {
		super(config, pageFetcher, robotstxtServer);
	}

	/**
	 * Start the crawling session and wait for it to finish.
	 *
	 * @param crawlerFactory
	 *            Factory that creates the class that implements the logic for
	 *            crawler threads
	 * @param numberOfCrawlers
	 *            the number of concurrent threads that will be contributing in
	 *            this crawling session.
	 * @param <T>
	 *            Your class extending WebCrawler
	 */
	public <T extends WebCrawler> void start(@Qualifier("crawlerFactory") final CrawlerFactory crawlerFactory, @Value("${numberOfCrawlers}") final int numberOfCrawlers) {
		this.start(crawlerFactory, numberOfCrawlers, true);
	}

	/**
	 * Start the crawling session and return immediately.
	 *
	 * @param crawlerFactory
	 *            Factory that creates the class that implements the logic for
	 *            crawler threads
	 * @param numberOfCrawlers
	 *            the number of concurrent threads that will be contributing in
	 *            this crawling session.
	 * @param <T>
	 *            Your class extending WebCrawler
	 */
	public <T extends WebCrawler> void startNonBlocking(@Qualifier("crawlerFactory") final CrawlerFactory crawlerFactory, @Value("${numberOfCrawlers}") final int numberOfCrawlers) {
		this.start(crawlerFactory, numberOfCrawlers, false);
	}

	/**
	 * Ugly hack to make Crawler4J usable by Spring, commit this to creator of
	 * project for review, otherwise have to reimplement this with every new
	 * release of their code
	 */
	protected <T extends WebCrawler> void start(@Qualifier("crawlerFactory") final CrawlerFactory crawlerFactory, @Value("${numberOfCrawlers}") final int numberOfCrawlers,
			boolean isBlocking) {
		try {
			finished = false;
			crawlersLocalData.clear();
			final List<Thread> threads = new ArrayList<Thread>();
			final List<T> crawlers = new ArrayList<T>();

			for (int i = 1; i <= numberOfCrawlers; i++) {
				T crawler = crawlerFactory.createCrawlerInstance();
				Thread thread = new Thread(crawler, "Crawler " + i);
				crawler.setThread(thread);
				crawler.init(i, this);
				thread.start();
				crawlers.add(crawler);
				threads.add(thread);
				logger.info("Crawler {} started", i);
			}

			final ExtendableCrawlerController controller = this;

			Thread monitorThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						synchronized (waitingLock) {

							while (true) {
								sleep(10);
								boolean someoneIsWorking = false;
								for (int i = 0; i < threads.size(); i++) {
									Thread thread = threads.get(i);
									if (!thread.isAlive()) {
										if (!shuttingDown) {
											logger.info("Thread {} was dead, I'll recreate it", i);
											T crawler = crawlerFactory.createCrawlerInstance();
											thread = new Thread(crawler, "Crawler " + (i + 1));
											threads.remove(i);
											threads.add(i, thread);
											crawler.setThread(thread);
											crawler.init(i + 1, controller);
											thread.start();
											crawlers.remove(i);
											crawlers.add(i, crawler);
										}
									} else if (crawlers.get(i).isNotWaitingForNewURLs()) {
										someoneIsWorking = true;
									}
								}
								if (!someoneIsWorking) {
									// Make sure again that none of the threads
									// are alive.
									logger.info("It looks like no thread is working, waiting for 10 seconds to make sure...");
									sleep(10);

									someoneIsWorking = false;
									for (int i = 0; i < threads.size(); i++) {
										Thread thread = threads.get(i);
										if (thread.isAlive() && crawlers.get(i).isNotWaitingForNewURLs()) {
											someoneIsWorking = true;
										}
									}
									if (!someoneIsWorking) {
										if (!shuttingDown) {
											long queueLength = frontier.getQueueLength();
											if (queueLength > 0) {
												continue;
											}
											logger.info("No thread is working and no more URLs are in queue waiting for another 10 seconds to make sure...");
											sleep(10);
											queueLength = frontier.getQueueLength();
											if (queueLength > 0) {
												continue;
											}
										}

										logger.info("All of the crawlers are stopped. Finishing the process...");
										// At this step, frontier notifies the
										// threads that were waiting for new
										// URLs and they should stop
										frontier.finish();
										for (T crawler : crawlers) {
											crawler.onBeforeExit();
											crawlersLocalData.add(crawler.getMyLocalData());
										}

										logger.info("Waiting for 10 seconds before final clean up...");
										sleep(10);

										frontier.close();
										docIdServer.close();
										pageFetcher.shutDown();

										finished = true;
										waitingLock.notifyAll();
										env.close();

										return;
									}
								}
							}
						}
					} catch (Exception e) {
						logger.error("Unexpected Error", e);
					}
				}
			});

			monitorThread.start();

			if (isBlocking) {
				waitUntilFinish();
			}

		} catch (Exception e) {
			logger.error("Error happened", e);
		}
	}

	public void start(String seed) {
		this.addSeed(seed);
		this.start(crawlerFactory, numberOfCrawlers);		
	}

}