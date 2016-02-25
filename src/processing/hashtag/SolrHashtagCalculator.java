package processing.hashtag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.primitives.Ints;

import common.Bookmark;
import common.SolrConnector;
import file.BookmarkReader;
import file.PredictionFileWriter;

public class SolrHashtagCalculator {

	private final static int LIMIT = 10;
	
	// Statics ----------------------------------------------------------------------------------------------------------------------		
		
	public static String predictSample(String sampleDir, String filename, String solrUrl) {		
		List<Set<String>> predictionValues = new ArrayList<Set<String>>();
		List<Set<String>> realValues = new ArrayList<Set<String>>();
		SolrConnector trainConnector = new SolrConnector(solrUrl, filename + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, filename + "_test");
		
		Map<String, Set<String>> tweets = testConnector.getTweets();
		for (Map.Entry<String, Set<String>> tweet : tweets.entrySet()) {
			Map<String, Double> map = trainConnector.getTopHashtagsForTweetText(tweet.getKey(), LIMIT);
			predictionValues.add(map.keySet());
			realValues.add(tweet.getValue());
			if (predictionValues.size() % 100 == 0) {
				System.out.println(predictionValues.size() + " users done. Left ones: " + (tweets.size() - predictionValues.size()));
			}
		}
		
		String suffix = "solrht";
		PredictionFileWriter.writeSimplePredictions(predictionValues, realValues, sampleDir + "/" + filename + "_" + suffix);
		return suffix;
	}
	
	public static String predictTrainSample(String sampleDir, String filename, String solrUrl, boolean hours, Integer recentTweetThreshold) {		
		List<Set<String>> predictionValues = new ArrayList<Set<String>>();
		List<Set<String>> realValues = new ArrayList<Set<String>>();
		SolrConnector trainConnector = new SolrConnector(solrUrl, filename + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, filename + "_test");
		
		String suffix = "";
		Map<String, Set<String>> userIDs = testConnector.getUserIDs();		
		for (Map.Entry<String, Set<String>> user : userIDs.entrySet()) {
			Map<String, Double> map = null;
			if (recentTweetThreshold == null) {
				String id = trainConnector.getMostRecentTweetOfUser(user.getKey());
				map = trainConnector.getTopHashtagsForTweetID(id, LIMIT);
				suffix = "solrht_train";
			} else {
				String text = null;
				if (hours) {
					text = trainConnector.getTweetTextOfLastHours(user.getKey(), recentTweetThreshold.intValue());
					suffix = "solrht_train_" + recentTweetThreshold.intValue() + "hours";
				} else {
					text = trainConnector.getTweetTextOfRecentTweets(user.getKey(), recentTweetThreshold.intValue());
					suffix = "solrht_train_" + recentTweetThreshold.intValue();
				}
				map = trainConnector.getTopHashtagsForTweetText(text, LIMIT);
			}
			predictionValues.add(map.keySet());
			realValues.add(user.getValue());
			if (predictionValues.size() % 100 == 0) {
				System.out.println(predictionValues.size() + " users done. Left ones: " + (userIDs.size() - predictionValues.size()));
			}
		}
		
		PredictionFileWriter.writeSimplePredictions(predictionValues, realValues, sampleDir + "/" + filename + "_" + suffix);
		return suffix;
	}
	
	public static Map<Integer, Map<Integer, Double>> getNormalizedHashtagPredictions(String filename, String solrUrl, BookmarkReader reader) {		
		SolrConnector trainConnector = new SolrConnector(solrUrl, filename + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, filename + "_test");
		Map<Integer, Map<Integer, Double>> hashtagMaps = new LinkedHashMap<Integer, Map<Integer, Double>>();
		
		List<Tweet> tweets = testConnector.getTweetObjects();
		for (Tweet tweet : tweets) {
			Map<Integer, Double> normalizedIntResult = new LinkedHashMap<Integer, Double>();
			Map<String, Double> stringResult = trainConnector.getTopHashtagsForTweetText(tweet.getText(), 100);
			double denom = 0.0;
			for (Map.Entry<String, Double> e : stringResult.entrySet()) {
				denom += Math.exp(e.getValue());
			}
			for (Map.Entry<String, Double> e : stringResult.entrySet()) {
				normalizedIntResult.put(reader.getTagMap().get(e.getKey()), Math.exp(e.getValue()) / denom);
			}
			
			hashtagMaps.put(reader.getUserMap().get(tweet.getUserid()), normalizedIntResult);
		}
		
		return hashtagMaps;
	}
}
