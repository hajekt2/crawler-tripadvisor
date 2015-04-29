package it.thecrawlers;

import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.crawler.dao.LocationDAO;
import it.thecrawlers.crawler.ta.ReviewService;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
public class ReviewServiceTest2 {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	@Autowired
	ReviewService reviewService;
	@Autowired
	ItemDAO itemDao;
	@Autowired
	LocationDAO locationDao;
	
	@Test
	public void runTestFolderTest() throws IOException {
		File inputFolder = new File("D:\\temp\\crawldata");
		ArrayList<File> fileList = new ArrayList<File>(Arrays.asList(inputFolder.listFiles()));
		int position = 1;
		for (File file : fileList) {
			logger.debug("Processing file {} of {} : {}", position, fileList.size(), file.getName());
			String path = "/Hotel_Review" + file.getName();
			reviewService.processReviewPage(path, path, FileUtils.readFileToString(file));
			position ++;
		}
		reviewService.processReviewsForAllItems();
	}
}
