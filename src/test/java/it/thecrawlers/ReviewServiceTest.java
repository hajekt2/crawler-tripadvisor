package it.thecrawlers;

import static org.junit.Assert.assertEquals;
import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.crawler.dao.LocationDAO;
import it.thecrawlers.crawler.ta.ReviewService;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Location;
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
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ReviewServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	private String reviewFileName = "_Hotel_Review-g1006204-d2440155-Reviews-Park_Inn_Trysil_Mountain_Resort-Trysil_Municipality_Hedmark_Eastern_Norway.html";
	private String location2FileName = "_Hotel_Review-g190502-d206868-Reviews-Radisson_Blu_Royal_Hotel_Bergen-Bergen_Hordaland_Western_Norway.html";
	
	@Autowired
	ReviewService reviewService;
	@Autowired
	ItemDAO itemDao;
	@Autowired
	LocationDAO locationDao;
	
	@Test
	public void processReviewPageTest() throws IOException {
		URL testFileUrl = getClass().getResource("/" + reviewFileName);

		reviewService.processReviewPage("/Hotel_Review"+reviewFileName, "/Hotel_Review"+reviewFileName, FileUtils.readFileToString(new File(testFileUrl.getPath())));
		Iterable<Item> result = itemDao.findAll();
		assertEquals(1, Iterables.size(result));
		Item item = Iterables.get(result, 0);
		logger.debug(item.toString());
		
	}

	@Test
	@Transactional
	public void processLocationsTest() throws IOException {
		URL reviewFileUrl = getClass().getResource("/" + reviewFileName);
		URL location2FileUrl = getClass().getResource("/" + location2FileName);

		Location processedLocation = reviewService.processLocations("/Hotel_Review"+reviewFileName, FileUtils.readFileToString(new File(reviewFileUrl.getPath())));
		locationDao.save(processedLocation);
		
		Location readLocation = locationDao.findOne(processedLocation.getId());		
		logger.debug(readLocation.toString());		
		assertEquals("g1006204", readLocation.getId());
		assertEquals("g190459", readLocation.getParent().getId());
		
		Location processedLocation2 = reviewService.processLocations("/Hotel_Review"+reviewFileName, FileUtils.readFileToString(new File(reviewFileUrl.getPath())));
		logger.debug(processedLocation2.toString());		
		assertEquals("g1006204", processedLocation2.getId());
		assertEquals("g190459", processedLocation2.getParent().getId());
		locationDao.save(processedLocation2);
		
		Location processedLocation3 = reviewService.processLocations("/Hotel_Review"+location2FileName, FileUtils.readFileToString(new File(location2FileUrl.getPath())));
		logger.debug(processedLocation3.toString());		
		assertEquals("g190502", processedLocation3.getId());
		assertEquals("g190501", processedLocation3.getParent().getId());
		locationDao.save(processedLocation2);
		
	}	
}
