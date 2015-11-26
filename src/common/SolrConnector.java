package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrConnector {
	
	private SolrServer server;
	
	public SolrConnector(String core) {
		this.server = new HttpSolrServer("http://localhost:8983/solr/" + core);
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
		
		SolrQuery solrParams = new SolrQuery();
		solrParams.set("q", "text:" + tweetText);
		solrParams.set("fl", "hashtags,score");
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
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		return hashtagMap;
	}
}
