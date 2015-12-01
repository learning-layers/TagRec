package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MoreLikeThisParams;

public class SolrConnector {
	
	private SolrServer server;
	
	public SolrConnector(String solrUrl, String core) {
		this.server = new HttpSolrServer(solrUrl + "/solr/" + core);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Set<String>> getTweets() {
		Map<String, Set<String>> tweets = new LinkedHashMap<String, Set<String>>();
		
		SolrQuery solrParams = new SolrQuery();
		solrParams.set("q", "*:*");
		solrParams.set("fl", "text,hashtags");
		solrParams.set("rows", Integer.MAX_VALUE);
		QueryResponse r = null;
		try {
			r = this.server.query(solrParams);
			SolrDocumentList docs = r.getResults();
			for (SolrDocument d : docs) {
				tweets.put((String) d.get("text"), new HashSet<String>((List<String>) d.get("hashtags")));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		return tweets;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Double> getTopHashtagsForTweetText(String tweetText, int limit) {	
		Map<String, Double> hashtagMap = new LinkedHashMap<String, Double>();
		String cleanedTweetText = getCleanedTweetText(tweetText);
		if (cleanedTweetText.isEmpty()) {
			return hashtagMap;
		}
		
		SolrQuery solrParams = new SolrQuery();
		// query version
		//solrParams.set("q", "text:" + cleanedTweetText);
		// mlt version
		solrParams.setQueryType("/mlt");
		solrParams.set("stream.body", cleanedTweetText);
		solrParams.set("mlt.fl", "text");
		// additional parameters
		solrParams.set("fl", "hashtags,score");
		solrParams.set("mlt.count", 20);
		solrParams.set("rows", 20);
		QueryResponse r = null;
		try {
			r = this.server.query(solrParams);
			SolrDocumentList docs = r.getResults();
			for (SolrDocument d : docs) {
				double score = (float) d.get("score");
				Set<String> hashtags = new HashSet<String>((List<String>) d.get("hashtags"));
				for (String h : hashtags) {
					if (hashtagMap.size() < limit) {
						if (!hashtagMap.containsKey(h)) {
							hashtagMap.put(h, score);
						}
					}
				}
				if (hashtagMap.size() >= limit) {
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Exception with tweet-text: " + cleanedTweetText);
			e.printStackTrace();
		}
		
		return hashtagMap;
	}
	
	private String getCleanedTweetText(String tweetText) {
		if (tweetText != null) {
			return tweetText.replaceAll("[^a-zA-Z0-9 ]+", "").trim();
		}
		return "";
	}
}
