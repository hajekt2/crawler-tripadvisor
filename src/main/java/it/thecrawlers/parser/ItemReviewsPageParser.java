package it.thecrawlers.parser;

import it.thecrawlers.model.Item;
import it.thecrawlers.model.ItemType;
import it.thecrawlers.model.Review;
import it.thecrawlers.model.ReviewValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ItemReviewsPageParser {
	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);
	
	public boolean isItemReviewsPage(String path) {
		return (path.startsWith("/Hotel_Review"))
				|| (path.startsWith("/Restaurant_Review"))
				|| (path.startsWith("/Attraction_Review"));
	}

	public Item parseItem(String html, String path) {
		Source source = new Source(html); // creo

		String[] pathFields = path.split("-");
		/*
		 * NB:
		 * " /Restaurant_Review-g1207908-d2060003-Reviews-or20-Mama_Rosa_s-Leighton_Buzzard_Bedfordshire_England.html"
		 * [0] /Restaurant_Review [1] g1207908 [2] d2060003 [3] Reviews [4?]
		 * or20 [4-5] Mama_Rosa_s (penultimo) [5-6]
		 * Leighton_Buzzard_Bedfordshire_England.html (ultimo)
		 */
		String itemID = pathFields[2];
		String description = pathFields[pathFields.length - 2]
				.replace("_", " ");
		// String description = source.getElementById("HEADING").getContent().toString();

		// String locationID = pathFields[1];
		// String locationName = pathFields[pathFields.length - 1].substring(0, IndexOf(".") - 1);
		String raw_totalReviewsCount = "0";
		int totalReviewsCount = 0;
		try {
			raw_totalReviewsCount = source
					.getFirstElementByClass("reviews_header").getContent()
					.toString().replace(".", "");
			totalReviewsCount = Integer.parseInt(raw_totalReviewsCount
					.substring(0, raw_totalReviewsCount.indexOf(" ")));

		} catch (Exception ex) {}

		List<Element> reviewElems = source.getAllElementsByClass("reviewSelector");
		for (Element element : reviewElems) {
			String id = element.getAttributeValue("id");
			logger.debug(id);
		}
		
		Item item = new Item(itemID, description);
		item.setTotalReviewsCount(totalReviewsCount);

		String typeName = pathFields[0].substring(1, path.indexOf("_")); // es: "/Hotel_Review" >> "Hotel"
		if (typeName.equals("Hotel"))
			item.setType(ItemType.hotel);
		else if (typeName.equals("Restaurant"))
			item.setType(ItemType.restaurant);
		else if (typeName.equals("Attraction"))
			item.setType(ItemType.attraction);
		else
			System.out.println("Tipo di Item non riconosciuto, valore in input: '" + typeName + "'");
		return item;
	}

	public String parseItemIdFromPath(String path) {
		String[] pathFields = path.split("-");
		return pathFields[2];
	}

	public List<Review> parseReviews(String html, String path) {
		Source source = new Source(html); // creo la source di jericho
		List<Review> parsedReviews = new LinkedList<Review>();

		List<Element> reviews = source.getAllElementsByClass("reviewSelector");
		for (Element rev : reviews) {
			try {
				String reviewID = "r"
						+ rev.getAttributeValue("id").substring(7); // "review_1352800"
																	// -->
																	// "r1352800"

				if (rev.getChildElements().isEmpty()) continue;
				
				String raw_date = null;
				Element ratingDateElem = rev.getFirstElementByClass("ratingDate");
				if (ratingDateElem != null) {
					raw_date = ratingDateElem.getAttributeValue("title");
					if (StringUtils.isEmpty(raw_date)) {
		                raw_date = ratingDateElem.getContent().toString(); // ">Reviewed January 30, 2015",
		                raw_date = raw_date.substring(9);						
					}
				}
				Date date = null;
				if (!StringUtils.isEmpty(raw_date)) {
					date = parseRawDate(StringUtils.strip(raw_date));
				}

				// Potremmo voler estrarre anche il titolo della review (non funziona sempre)
				//estraggo "reviewTitle"
				/*
				 * String reviewTitle =
				 * rev.getFirstElementByClass("quote").getContent
				 * ().getFirstElement().getContent().toString(); reviewTitle =
				 * reviewTitle.substring(8, reviewTitle.length() - 8 ) ; //
				 * altrimenti ho &#x201c;_______&#x201d;
				 */
				String reviewTitle = "";  //altrimenti passo una stringa vuota come titolo della review

				String raw_value = rev
						.getFirstElementByClass("sprite-rating_s_fill")
						.getAttributeValue("alt"); // 4 of 5 stars
				
				int int_value = Integer.parseInt(raw_value.substring(0,
						raw_value.indexOf(" ")));

				if ((int_value < 1)||(int_value>5))
					throw new Exception("Il valore '" + int_value + "' non corrisponde ad un voto valido! (1-5)");
				
				ReviewValue rv = null;
				switch (int_value) {
				case 1:
					rv = ReviewValue.veryBad;
					break;
				case 2:
					rv = ReviewValue.bad;
					break;
				case 3:
					rv = ReviewValue.medium;
					break;
				case 4:
					rv = ReviewValue.good;
					break;
				case 5:
					rv = ReviewValue.veryGood;
					break;
				}
						
				Review newReview = new Review(reviewID, date, reviewTitle, rv);
				parsedReviews.add(newReview);
			} catch (Exception e) {
				logger.error("Parsing error on page ["+path+"]", e);
			}

		}
		return parsedReviews;
	}

	private Date parseRawDate(String raw_date) throws ParseException {
		//sample April 10, 2015
		return new SimpleDateFormat("MMM dd, yyyy",  Locale.ENGLISH).parse(raw_date);
	}
}
