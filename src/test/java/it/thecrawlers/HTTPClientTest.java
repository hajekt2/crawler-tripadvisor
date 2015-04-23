package it.thecrawlers;

import it.thecrawlers.utils.HttpClientPool;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;

public class HTTPClientTest {

	@Test
	@Ignore
	public void test() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet("http://www.tripadvisor.com/ExpandedUserReviews-g190479-d232475?target=261579208&context=0&reviews=261579208,257044075,253147891,252231612,252226997,252038101,251199763,247574986,245266990&servlet=Hotel_Review&expand=1");
			CloseableHttpResponse response1 = httpclient.execute(httpGet);
			try {
				System.out.println(response1.getStatusLine());
				HttpEntity entity1 = response1.getEntity();
				// do something useful with the response body
				// and ensure it is fully consumed
				System.out.println(IOUtils.toString(entity1.getContent()));
				EntityUtils.consume(entity1);
			} finally {
				response1.close();
			}

		} finally {
			httpclient.close();
		}
	}
	
	@Test
	public void testPool2() throws ClientProtocolException, IOException, InterruptedException {
		String response = HttpClientPool.query("http://www.tripadvisor.com/ExpandedUserReviews-g190479-d232475?target=261579208&context=0&reviews=261579208,257044075,253147891,252231612,252226997,252038101,251199763,247574986,245266990&servlet=Hotel_Review&expand=1"); 
		System.out.println(response);
		HttpClientPool.shutdown();
	}
	
}
