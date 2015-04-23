package it.thecrawlers;

import static org.junit.Assert.assertEquals;
import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

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

	@Autowired
	ItemReviewsPageParser parser;
	
	@Test
	public void testParser() throws IOException {
		URL testFileUrl = getClass().getResource("/_Hotel_Review-g1006204-d260111-Reviews-or10-Trysil_Knut_Hotell-Trysil_Municipality_Hedmark_Eastern_Norway.html");

		List<Review> parseReviews = parser.parseReviews(FileUtils.readFileToString(new File(testFileUrl.getPath())), testFileUrl.getFile());		
		assertEquals(1, parseReviews.size());
		logger.info(parseReviews.get(1).toString());		
	}

}
