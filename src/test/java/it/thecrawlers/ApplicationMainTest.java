package it.thecrawlers;

import org.junit.Test;


public class ApplicationMainTest {

	@Test
	public void testCrawl() throws Exception {
		String[] args = {"20", "D:/temp/crawl", "200"};
		ApplicationMain.main(args);
	}

}
