package it.thecrawlers;

import it.thecrawlers.crawler.ExtendableCrawlerController;
import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.crawler.ta.ReviewService;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Review;
import it.thecrawlers.utils.HttpClientPool;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationMain {

	static final Logger logger = LoggerFactory.getLogger(ApplicationMain.class);

	private class CmdLineOptions {	   
	    @Option(name="-m", required=true, usage="mode {"
	    		+ "crawl - crawl hotel review to disc, "
	    		+ "crawlf - crawl full reviews based on hotel reviews, "
	    		+ "writecsv - write full hotel reviews to CSV file")    
	    private String mode;	    
	    @Option(name="-startUrl",usage="start page URL")
	    private String startUrl = "http://no.tripadvisor.com/Hotels-g1006204-Trysil_Municipality_Hedmark_Eastern_Norway-Hotels.html";	    
	    @Option(name="-i",usage="input folder")
	    private File inputFolder = new File("c:\\temp\\crawldata");	    
	    @Option(name="-enc",usage="input file encoding - Windows-1252, UTF-8, ...")
	    private String encoding = "UTF-8";
	    @Option(name="-csv",usage="output csv file")
	    private File outputCsvFile = new File("c:\\temp\\reviews.csv");	    
	}

	public void run(String[] args) {
		CmdLineOptions bean = new CmdLineOptions();
	    CmdLineParser parser = new CmdLineParser(bean);
		try {
		    parser.parseArgument(args);
		} catch( CmdLineException e ) {
		    System.err.println(e.getMessage());
		    System.err.println("java -jar crawler-tripadvisor.jar [options...] arguments...");
		    parser.printUsage(System.err);
		    return;
		}		
		
		logger.info("Running in {} mode", bean.mode);
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");				
		ctx.registerShutdownHook();
		
		if (bean.mode.equalsIgnoreCase("crawl")) {
			ExtendableCrawlerController controller = BeanFactoryUtils.beanOfType(ctx, ExtendableCrawlerController.class);
			try {
				controller.start(bean.startUrl);
			} catch (Exception e) {
				logger.error("Crawler initializaton failed", e);
			}
			return;
		}
		
		if (bean.mode.equalsIgnoreCase("crawlf")) {
			if (bean.inputFolder == null) {
				logger.error("Input folder parameter cannot be empty");
				return;
			}
			
			ReviewService reviewService = BeanFactoryUtils.beanOfType(ctx, ReviewService.class);
			
	        ArrayList<File> fileList = new ArrayList<File>(Arrays.asList(bean.inputFolder.listFiles()));
	        int position = 1;
	        for (File file : fileList) {
	            logger.debug("Processing file {} of {} : {}", position, fileList.size(), file.getName());
	            String path = "/Hotel_Review" + file.getName();
	            try {
					reviewService.processReviewPage(path, path, FileUtils.readFileToString(file, bean.encoding));
				} catch (IOException e) {
					logger.error("Cannot read file "+file.getName(), e);
				}
	            position ++;
	        }			
		}

		if (bean.mode.equalsIgnoreCase("writecsv")) {
			if (bean.outputCsvFile == null) {
				logger.error("CSV file parameter cannot be empty");
				return;
			}

			String START_SEP = "\"";
			String SEP = "\",\"";
			String END_SEP = "\"" + System.getProperty("line.separator");
			ItemDAO itemDao = BeanFactoryUtils.beanOfType(ctx, ItemDAO.class);
			
			logger.info("Writing DB content into file: {}", bean.outputCsvFile.getAbsolutePath());

			try {
				FileUtils.write(bean.outputCsvFile, START_SEP + 
						"reviewId" + SEP + 
						"author" + SEP + 
						"date" + SEP + 
						"rating" + SEP +
						"title" + SEP + 
						"text" + SEP + 
						"hotelId" + SEP + 
						"hotelName" + SEP + 
						"locationId" + SEP +
						"locationName" + SEP + 
						"fullLocationName" + END_SEP, bean.encoding, false);

				Iterable<Item> items = itemDao.findAll();
				for (Item item : items) {
					for (Review review : item.getReviews()) {
		    			try {
		    				FileUtils.write(bean.outputCsvFile, START_SEP+
		    					review.getId()+SEP+
		    					review.getAuthor().replaceAll("\\r\\n|\\r|\\n", " ")+SEP+
		    					new SimpleDateFormat("yyyy-mm-dd").format(review.getDate())+SEP+
		    					review.getRating()+SEP+
		    					review.getTitle().replaceAll("\\r\\n|\\r|\\n", " ")+SEP+
		    					review.getText().replaceAll("\\r\\n|\\r|\\n", " ")+SEP+
		    					item.getId()+SEP+
		    					item.getName()+SEP+
		    					item.getLocation().getId()+SEP+
		    					item.getLocation().getName()+SEP+
		    					item.getLocation().getFullName()+END_SEP, bean.encoding, true);
		    			} catch (Exception e) {
		    				logger.error("Error reading review id = "+review.getId(), e);
		    			}
		    				
					}

				}
				logger.info("Writing completed");
			} catch (IOException e) {
				logger.error("Processing error", e);
			}			
			
		}
		
		try {
			HttpClientPool.shutdown();
		} catch (InterruptedException e) {
		}
		
	}
	
	public static void main(String[] args) {
		new ApplicationMain().run(args);
	}

}
