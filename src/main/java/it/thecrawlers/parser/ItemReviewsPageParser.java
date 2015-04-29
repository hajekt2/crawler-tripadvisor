package it.thecrawlers.parser;

import it.thecrawlers.model.Item;
import it.thecrawlers.model.Review;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ItemReviewsPageParser {
	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	public boolean isItemReviewsPage(String path) {	
		boolean result = (path.startsWith("/Hotel_Review"))
				|| (path.startsWith("/Restaurant_Review"))
				|| (path.startsWith("/Attraction_Review"));
		logger.trace("Path is a review page = {} for {}", result, path);
		return result;
	}

	public Item parseItem(String html, String path, String url) {
		Source source = new Source(html);
		Item item = new Item();

		String[] pathFields = path.split("-");
		/*
		 * " /Restaurant_Review-g1207908-d2060003-Reviews-or20-Mama_Rosa_s-Leighton_Buzzard_Bedfordshire_England.html"
		 * [0] /Restaurant_Review [1] g1207908 [2] d2060003 [3] Reviews [4?]
		 * or20 [4-5] Mama_Rosa_s [5-6]
		 */
		item.setId(pathFields[2]);
		item.setName(source.getElementById("HEADING").getContent().getRenderer().toString());
		if (StringUtils.isEmpty(item.getName())) {
			item.setName(pathFields[pathFields.length - 2].replace("_", " "));
		}
		item.setUrl(url);	
		item.setCrawlDate(new Date());
		return item;
	}

	/*	    Location example:
	 * 		<ul class="breadcrumbs" id="BREADCRUMBS">
		      <li itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb">
		        <a href="/Tourism-g4-Europe-Vacations.html" itemprop="url"
		        onclick="ta.setEvtCookie(&#39;Breadcrumbs&#39;, &#39;click&#39;, &#39;Continent&#39;, 1, this.href);">
		          <span itemprop="title">Europa</span>
		        </a>
		        <span class="separator">
		          <img src="http://e2.tacdn.com/img2/x.gif" class="sprite-arrow_green_right" alt="" />
		        </span>
		      </li>
		      <li itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb">
		        <a href="/Tourism-g190455-Norway-Vacations.html" itemprop="url"
		        onclick="ta.setEvtCookie(&#39;Breadcrumbs&#39;, &#39;click&#39;, &#39;Country&#39;, 2, this.href);">
		          <span itemprop="title">Norge</span>
		        </a>
		        <span class="separator">
		          <img src="http://e2.tacdn.com/img2/x.gif" class="sprite-arrow_green_right" alt="" />
		        </span>
		      </li>
		    </ul>
	*/		
	public LinkedMap<String, String> parseLocations(String html, String path) {
		Source source = new Source(html);
		LinkedMap<String, String> locationMap = new LinkedMap<String, String>();

		List<Element> locationLiElements = source.getElementById("BREADCRUMBS").getChildElements();
		for (Element element : locationLiElements) {
			String locationName = element.getContent().getRenderer().toString();
			String href = element.getChildElements().get(0).getAttributeValue("href");
			String[] hrefSplit = href.split("-");
			String locationId = hrefSplit[1];
			if (!locationMap.containsKey(locationId))
				locationMap.put(locationId, locationName);
		}		
		return locationMap;
	}
	
	public Set<Review> parseReviews(String html, String path) {
		Source source = new Source(html);
		Set<Review> parsedReviews = new HashSet<Review>();

		List<Element> reviewsElementList = source.getAllElementsByClass("reviewSelector");
		for (Element reviewElement : reviewsElementList) {
			Review review = new Review();
			try {								
				review.setId(reviewElement.getAttributeValue("id").substring(7)); // "review_1352800"
				if (reviewElement.getChildElements().isEmpty()) { 
					//do no add such review - review is written in other language					
					continue;
				}

				review.setCrawlDate(new Date());			
				review.setAuthor(getReviewAuthor(reviewElement));
				review.setRating(getReviewRating(reviewElement, review));				
				review.setTitle(getReviewTitle(reviewElement));				
				review.setDate(getReviewCreationDate(reviewElement));
				parsedReviews.add(review);
			} catch (Exception e) {
				logger.error("Parsing error on page ["+path+"]", e);
			}

		}
		return parsedReviews;
	}

	public void parseExpandedUserReview(String expandedUserReviewHtml, Set<Review> reviews) {
		Source source = new Source(expandedUserReviewHtml);

		for (Iterator<Review> iterator = reviews.iterator(); iterator.hasNext();) {
			Review review = iterator.next();
			try {
				Element reviewElement = source.getElementById("expanded_review_" + review.getId());
				review.setCrawlDate(new Date());
				review.setAuthor(getReviewAuthor(reviewElement));
				review.setRating(getReviewRating(reviewElement, review));
				review.setTitle(getReviewTitle(reviewElement));
				review.setText(getReviewText(reviewElement));
				review.setDate(getReviewCreationDate(reviewElement));
			} catch (Exception e) {
				logger.error("Parsing error on review ["+review.getId()+"] ... removing it", e);
				iterator.remove();
			}
		}		
	}

	private String getReviewText(Element reviewElement) {
		return reviewElement.getFirstElementByClass("entry").getContent()
				.getFirstElement().getContent().getRenderer().toString();
	}

	private String getReviewTitle(Element reviewElement) {
		String title = reviewElement.getFirstElementByClass("quote").getContent()
				.getFirstElement().getContent().getRenderer().toString();
		//removes quotes
		title = title.substring(1, title.length() - 1);
		return title;
	}

	private String getReviewAuthor(Element reviewElement) {
		return reviewElement.getFirstElementByClass("username").getContent().getRenderer().toString().trim();
	}

	private Date getReviewCreationDate(Element reviewElement) throws ParseException {
		String raw_date = null;
		Element ratingDateElem = reviewElement.getFirstElementByClass("ratingDate");
		if (ratingDateElem != null) {
			raw_date = ratingDateElem.getAttributeValue("title");
			if (StringUtils.isEmpty(raw_date)) {
                raw_date = ratingDateElem.getContent().toString(); // Anmeldt 10 mars 2015
                raw_date = raw_date.substring(8);						
			}					
		}
		Date date = null;
		if (!StringUtils.isEmpty(raw_date)) {
			date = parseRawDate(StringUtils.strip(raw_date));
		}
		return date;
	}
	
	private int getReviewRating(Element reviewElement, Review review) {
		String rawRatingValue = reviewElement
				.getFirstElementByClass("sprite-rating_s_fill")
				.getAttributeValue("alt"); // 3 av 5 stjerner			
		int ratingValue = Integer.parseInt(rawRatingValue.substring(0,
				rawRatingValue.indexOf(" ")));
		if ((ratingValue < 1)||(ratingValue > 5)) {
			logger.error("Rating value {} is not in allowed limit (1-5) for review {}", ratingValue, review.getId());
			ratingValue = -1;
		}
		return ratingValue;
	}

	private Date parseRawDate(String raw_date) throws ParseException {
		//sample: 20 april 2015
		return new SimpleDateFormat("dd MMM yyyy",  new Locale("no")).parse(raw_date);
	}

}
