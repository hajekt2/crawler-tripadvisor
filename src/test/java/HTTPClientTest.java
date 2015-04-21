import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class HTTPClientTest {

	@Test
	public void test() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet("http://www.tripadvisor.com/ExpandedUserReviews-g190479-d232475?target=261579208&context=0&reviews=261579208,257044075,253147891,252231612,252226997,252038101,251199763,247574986,245266990&servlet=Hotel_Review&expand=1");
			CloseableHttpResponse response1 = httpclient.execute(httpGet);
			// The underlying HTTP connection is still held by the response
			// object
			// to allow the response content to be streamed directly from the
			// network socket.
			// In order to ensure correct deallocation of system resources
			// the user MUST call CloseableHttpResponse#close() from a finally
			// clause.
			// Please note that if response content is not fully consumed the
			// underlying
			// connection cannot be safely re-used and will be shut down and
			// discarded
			// by the connection manager.
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

}
