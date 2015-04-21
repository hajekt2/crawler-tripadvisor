import static org.junit.Assert.*;
import it.thecrawlers.crawler.CrawlHandler;

import org.junit.Test;


public class CrawlHandlerTest {

	@Test
	public void testCrawl() throws Exception {
		String[] args = {"20", "D:/temp/crawl", "200"};
		CrawlHandler.main(args);
	}

}
