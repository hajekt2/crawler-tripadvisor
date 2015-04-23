package it.thecrawlers.crawler.ta;

import it.thecrawlers.crawler.CrawlHandler;
import it.thecrawlers.model.Item;
import it.thecrawlers.model.Review;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

@Component
public class TACrawlHandlerImpl implements CrawlHandler {

	static final Logger logger = LoggerFactory.getLogger(TACrawlHandlerImpl.class);

	@Autowired
	private CrawlController crawlController;
	
	@Autowired
	private ItemReviewsPageParser parser;

	private Map<String, Item> idToItem = new HashMap<String, Item>();
	private int reviewCount = 0;

	@Override
	public void processPage(Page page) {
		String path = page.getWebURL().getPath();
		
		if (isItemReviewsPage(path) ){
			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				String html = htmlParseData.getHtml();

//				String fileNameFormPath = path.replaceAll("[^a-zA-Z0-9.-]", "_");				
//				try {
//					FileUtils.writeStringToFile(new File("d:/temp/crawldata", fileNameFormPath), html);
//				} catch (IOException e) {
//					logger.error("Cannot write page to the file", e);;
//				}
				
//				String idItem = handler.parseItemIdFromPath(path);
//				List<Review> parsedReviews = handler.parseReviews(html,path);
//				Item item = handler.getItemById(idItem);	
//				@SuppressWarnings("unused")
//				DAO<Item> itemDao = new DAO<Item>();
//				@SuppressWarnings("unused")
//				ReviewDAO reviewDao = new ReviewDAO();
//				
//				if(item!=null){
//					item.getReviews().addAll(parsedReviews);
//					if (item.getTotalReviewsCount() == item.getReviews().size()){
////						handler.countCompleti++;						
//						System.out.println(" Item trovati finora: "+handler.getReviewCount());
//					}
//					
//				}
//				else
//				{
//					handler.incReviewCount();
//					item = handler.parseItem(html,path);
//					item.getReviews().addAll(parsedReviews);
//					handler.addNewItem(item);
//					
////					if (item.getTotalReviewsCount() == item.getReviews().size()){  						
////						handler.countCompleti++;
////					}
//					System.out.println(" Item trovati finora: "+handler.getReviewCount());
//					
//					int reviewsCount = item.getTotalReviewsCount();
//					int reviewsPageCount = reviewsCount / 10 ;
//					List<WebURL> reviewsURLs = new ArrayList<WebURL>(reviewsPageCount);
//					DocIDServer docIdServer= this.getMyController().getDocIdServer(); //serve per generare un docID
//					
//					for (int pagenum=0; pagenum<=reviewsPageCount; pagenum++){
//						String original_url = page.getWebURL().getURL();
//						String url;
//						url = original_url.replaceFirst("-Reviews(-or[0-9]+)?-", "-Reviews-or" + (pagenum*10) + "-");
//						WebURL webUrl = new WebURL();
//						webUrl.setURL(url);
//						webUrl.setPath(url.substring(url.lastIndexOf("/") - 1));
//						webUrl.setParentDocid(page.getWebURL().getDocid());
//						webUrl.setParentUrl(page.getWebURL().getURL());
//						webUrl.setDepth( (short)(page.getWebURL().getDepth()+1) );
//						webUrl.setDocid(docIdServer.getNewDocID(url));
//						reviewsURLs.add(webUrl);				
//					}
//					this.getMyController().getFrontier().scheduleAll(reviewsURLs);
//				}
				
			}
		}
		
	}

	private Map<String, Item> getIdToItem() {
		return idToItem;
	}

	private void setIdToItem(Map<String, Item> idToItem) {
		this.idToItem = idToItem;
	}

	private Item getItemById(String id) {
		return this.idToItem.get(id);
	}

	private void addNewItem(Item item) {
//		String id = item.getItemId();
//		System.out.println(id);
//		this.idToItem.put(id, item);
	}

	private List<Review> parseReviews(String html, String path) {
		return this.parser.parseReviews(html, path);
	}

	private Item parseItem(String html, String path) {
		return this.parser.parseItem(html, path);
	}

	private String parseItemIdFromPath(String path) {
		return this.parser.parseItemIdFromPath(path);
	}

	private boolean isItemReviewsPage(String path) {
		return this.parser.isItemReviewsPage(path);
	}

	private int getReviewCount() {
		return reviewCount;
	}

	private void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	private int incReviewCount() {
		return reviewCount++;
	}
}
