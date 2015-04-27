package it.thecrawlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ItemReviewsPageParserTest {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	private String reviewFileName = "_Hotel_Review-g1006204-d2440155-Reviews-Park_Inn_Trysil_Mountain_Resort-Trysil_Municipality_Hedmark_Eastern_Norway.html";
	
	@Autowired
	ItemReviewsPageParser parser;
	
	@Test
	public void parseItemTest() throws IOException {
		URL testFileUrl = getClass().getResource("/" + reviewFileName);

		Item item = parser.parseItem(FileUtils.readFileToString(new File(testFileUrl.getPath())), reviewFileName, reviewFileName);
		logger.debug(item.toString());
		assertEquals("d2440155", item.getId());
	}

	@Test
	public void parseReviewsTest() throws IOException {
		URL testFileUrl = getClass().getResource("/" + reviewFileName);

		Set<Review> parseReviews = parser.parseReviews(FileUtils.readFileToString(new File(testFileUrl.getPath())), reviewFileName);		
		logger.debug(Arrays.toString(parseReviews.toArray()));		
		assertEquals(10, parseReviews.size());
	}
	
	@Test
	public void parseExpandedUserReviewTest() throws IOException {
		URL testFileUrl = getClass().getResource("/ExpandedUserReview1.html");

		Set<Review> reviews = new HashSet<Review>();
		Review r = new Review();
		r.setId("264384292");
		reviews.add(r);
		r = new Review();
		r.setId("263525319");
		reviews.add(r);
		r = new Review();
		r.setId("263184876");
		reviews.add(r);
		r = new Review();
		r.setId("262844528");
		reviews.add(r);
		r = new Review();
		r.setId("261730429");
		reviews.add(r);
		r = new Review();
		r.setId("259352562");
		reviews.add(r);
		r = new Review();
		r.setId("258836251");
		reviews.add(r);
		r = new Review();
		r.setId("257397794");
		reviews.add(r);
		r = new Review();
		r.setId("252347150");
		reviews.add(r);
		r = new Review();
		r.setId("250992709");
		reviews.add(r);

		parser.parseExpandedUserReview(FileUtils.readFileToString(new File(testFileUrl.getPath())), reviews);
		
		logger.debug(Arrays.toString(reviews.toArray()));		
		assertEquals(10, reviews.size());
		for (Review review : reviews) {
			assertNotNull(review.getText());			
		}
	}
	
}
