package it.thecrawlers.crawler.ta;

import it.thecrawlers.utils.HttpClientPool;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExpandedUserReviewFetcher {

	private static final Logger logger = LoggerFactory.getLogger(ExpandedUserReviewFetcher.class);

	public String getExpandedUserReview(String itemId, String locationId, ArrayList<String> reviewIds) throws IOException {
		if (reviewIds.isEmpty()) return null;
		String url = "http://no.tripadvisor.com/ExpandedUserReviews-"+locationId+"-"+itemId+"?target="+reviewIds.get(0)+"&context=0&reviews="+StringUtils.join(reviewIds, ",")+"&servlet=Hotel_Review&expand=1";
		logger.trace("Full Review query URL: {}", url);
		return HttpClientPool.query(url);
	}
	
}
