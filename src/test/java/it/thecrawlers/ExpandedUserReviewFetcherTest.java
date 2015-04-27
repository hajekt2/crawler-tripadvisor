package it.thecrawlers;

import it.thecrawlers.crawler.ta.ExpandedUserReviewFetcher;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ExpandedUserReviewFetcherTest {

	private static final Logger logger = LoggerFactory.getLogger(ExpandedUserReviewFetcherTest.class);
	
	@Autowired
	ExpandedUserReviewFetcher fetcher;
	
	@Test
	public void fetchTest() throws IOException {
		ArrayList<String> reviewIds = new ArrayList<String>();
		reviewIds.add("264384292");
		reviewIds.add("263525319");
		reviewIds.add("263184876");
		reviewIds.add("262844528");
		reviewIds.add("261730429");
		reviewIds.add("259352562");
		reviewIds.add("258836251");
		reviewIds.add("257397794");
		reviewIds.add("252347150");
		reviewIds.add("250992709");		
		String fullReviews = fetcher.getExpandedUserReview("d2440155", "g1006204", reviewIds);
		logger.debug(fullReviews);
	}

}
