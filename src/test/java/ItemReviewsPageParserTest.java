import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ItemReviewsPageParserTest {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);
	
	@Test
	public void testParser() throws IOException {
		ItemReviewsPageParser parser = new ItemReviewsPageParser();
		
		List<Review> reviews = new ArrayList<Review>();
		
		Iterator<File> iterateFiles = FileUtils.iterateFiles(new File("D:/temp/crawldata"), null, false);
		while (iterateFiles.hasNext()) {
		    File file = iterateFiles.next();
		    reviews.addAll(parser.parseReviews(FileUtils.readFileToString(file), file.getName()));
		}

		logger.info("Size= {}", reviews.size());
		logger.info(reviews.get(1).toString());
		
	}

}
