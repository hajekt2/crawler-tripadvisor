package it.thecrawlers.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpClientPool {
	private static final Logger log = LoggerFactory.getLogger(HttpClientPool.class);

	// Single-element enum to implement Singleton.
	private static enum Singleton {
		// Just one of me so constructor will be called once.
		Client;
		// The thread-safe client.
		private final CloseableHttpClient threadSafeClient;
		// The pool monitor.
		private final IdleConnectionMonitorThread monitor;

		// The constructor creates it - thus late
		private Singleton() {
			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
			// Increase max total connection to 200
			cm.setMaxTotal(200);
			// Increase default max connection per route to 20
			cm.setDefaultMaxPerRoute(20);
			// Build the client.
			threadSafeClient = HttpClients.custom().setConnectionManager(cm).build();
			// Start up an eviction thread.
			monitor = new IdleConnectionMonitorThread(cm);
			// Don't stop quitting.
			monitor.setDaemon(true);
			monitor.start();
		}

		public CloseableHttpClient get() {
			return threadSafeClient;
		}

	}

	public static CloseableHttpClient getClient() {
		// The thread safe client is held by the singleton.
		return Singleton.Client.get();
	}

	// Watches for stale connections and evicts them.
	private static class IdleConnectionMonitorThread extends Thread {
		// The manager to watch.
		private final PoolingHttpClientConnectionManager cm;
		// Use a BlockingQueue to stop everything.
		private final BlockingQueue<Stop> stopSignal = new ArrayBlockingQueue<Stop>(1);

		// Pushed up the queue.
		private static class Stop {
			// The return queue.
			private final BlockingQueue<Stop> stop = new ArrayBlockingQueue<Stop>(1);

			// Called by the process that is being told to stop.
			public void stopped() {
				// Push me back up the queue to indicate we are now stopped.
				stop.add(this);
			}

			// Called by the process requesting the stop.
			public void waitForStopped() throws InterruptedException {
				// Wait until the callee acknowledges that it has stopped.
				stop.take();
			}

		}

		IdleConnectionMonitorThread(PoolingHttpClientConnectionManager cm) {
			super();
			this.cm = cm;
		}

		@Override
		public void run() {
			try {
				// Holds the stop request that stopped the process.
				Stop stopRequest;
				// Every 5 seconds.
				while ((stopRequest = stopSignal.poll(5, TimeUnit.SECONDS)) == null) {
					// Close expired connections
					cm.closeExpiredConnections();
					// Optionally, close connections that have been idle too
					// long.
					cm.closeIdleConnections(60, TimeUnit.SECONDS);
					// Look at pool stats.
					log.trace("Stats: {}", cm.getTotalStats());
				}
				// Acknowledge the stop request.
				stopRequest.stopped();
			} catch (InterruptedException ex) {
				// terminate
			}
		}

		public void shutdown() throws InterruptedException {
			log.trace("Shutting down client pool");
			// Signal the stop to the thread.
			Stop stop = new Stop();
			stopSignal.add(stop);
			// Wait for the stop to complete.
			stop.waitForStopped();
			// Close the connection manager.
			cm.close();
			log.trace("Client pool shut down");
		}

	}

	public static void shutdown() throws InterruptedException {
		// Shutdown the monitor.
		Singleton.Client.monitor.shutdown();
	}

	public static String query(String url) throws IOException {
		return query(url, null);
	}
	
	public static String query(String url, String q) throws IOException {
		// The request.
		final HttpRequestBase request;
		if (q != null) {
			// Prepare the post.
			HttpPost postRequest = new HttpPost(url);
			// Get it all into a JSON string.
			StringEntity input = new StringEntity(q);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			// Use that one.
			request = postRequest;
		} else {
			// Just get.
			request = new HttpGet(url);
		}
		log.debug("> " + url + (q == null ? "" : " " + q));
		// Post it and wait.
		return readResponse(request, HttpClientPool.getClient().execute(request));
	}

	private static String readResponse(HttpRequestBase request, CloseableHttpResponse response)
			throws IOException {
		// What was read.
		String result = null;
		try {
			// What happened?
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// Roll out the results
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream content = entity.getContent();
					if (content != null) { 
						result = IOUtils.toString(content);
						content.close();
					}
					EntityUtils.consume(entity);
				}
			} else {
				// The finally below will clean up.
				throw new IOException("HTTP Response: " + response.getStatusLine().getStatusCode());
			}
		} finally {
			// Always close the response.
			response.close();
		}

		if (result == null) {
			log.trace("< {null}");
		} else {
			log.trace("< {}", result.toString());
		}
		return result;
	}

}