package it.thecrawlers;

import static org.junit.Assert.*;
import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.crawler.ta.ReviewService;
import it.thecrawlers.model.Item;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Iterables;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ReviewServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	private String reviewFileName = "_Hotel_Review-g1006204-d2440155-Reviews-Park_Inn_Trysil_Mountain_Resort-Trysil_Municipality_Hedmark_Eastern_Norway.html";

	@Autowired
	ReviewService reviewService;
	@Autowired
	ItemDAO itemDao;
	
	@Test
	public void processReviewPageTest() throws IOException {
		URL testFileUrl = getClass().getResource("/" + reviewFileName);

		reviewService.processReviewPage("/Hotel_Review"+reviewFileName, "/Hotel_Review"+reviewFileName, FileUtils.readFileToString(new File(testFileUrl.getPath())));
		Iterable<Item> result = itemDao.findAll();
		assertEquals(1, Iterables.size(result));
		Item item = Iterables.get(result, 0);
		logger.debug(item.toString());
		
	}

}
