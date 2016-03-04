package processing.hashtag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
		
	public static String predictSample(String sampleDir, String solrCore, String solrUrl) {		
		List<Set<String>> predictionValues = new ArrayList<Set<String>>();
		List<Set<String>> realValues = new ArrayList<Set<String>>();
		SolrConnector trainConnector = new SolrConnector(solrUrl, solrCore + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, solrCore + "_test");
		
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
		PredictionFileWriter.writeSimplePredictions(predictionValues, realValues, sampleDir + "/" + solrCore + "_" + suffix);
		return suffix;
	}
		
	public static Map<Integer, Map<Integer, Double>> getNormalizedHashtagPredictions(String sampleDir, String solrCore, String solrUrl, BookmarkReader reader) {		
		SolrConnector trainConnector = new SolrConnector(solrUrl, solrCore + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, solrCore + "_test");
		Map<Integer, Map<Integer, Double>> hashtagMaps = new LinkedHashMap<Integer, Map<Integer, Double>>();
		List<Set<String>> realValues = new ArrayList<Set<String>>();
		List<Set<String>> predictionValues = new ArrayList<Set<String>>();
		
		List<Tweet> tweets = testConnector.getTweetObjects();
		for (Tweet tweet : tweets) {
			Set<String> predTagIDs = new LinkedHashSet<String>();
			Map<Integer, Double> normalizedIntResult = new LinkedHashMap<Integer, Double>();
			Map<String, Double> stringResult = trainConnector.getTopHashtagsForTweetText(tweet.getText(), 50);
			double denom = 0.0;
			for (Map.Entry<String, Double> e : stringResult.entrySet()) {
				Integer tID = reader.getTagMap().get(e.getKey().toLowerCase());
				if (tID != null) {
					normalizedIntResult.put(tID.intValue(), e.getValue());
					predTagIDs.add(tID.toString());
					denom += Math.exp(e.getValue());
				}
			}
			for (Map.Entry<Integer, Double> e : normalizedIntResult.entrySet()) {
				e.setValue(Math.exp(e.getValue()) / denom);
			}
			
			Integer uID = reader.getUserMap().get(tweet.getUserid());
			if (uID != null) {
				hashtagMaps.put(uID.intValue(), normalizedIntResult);
				predictionValues.add(predTagIDs);
				if (hashtagMaps.size() % 100 == 0) {
					System.out.println(hashtagMaps.size() + " users done. Left ones: " + (tweets.size() - hashtagMaps.size()));
				}
				
				Set<String> tagIDs = new LinkedHashSet<String>();
				for (String t : tweet.getHashtags()) {
					Integer tID = reader.getTagMap().get(t.toLowerCase());
					if (tID != null) {
						tagIDs.add(tID.toString());
					}
				}
				realValues.add(tagIDs);
			}
		}
		
		printHashtagPrediction(hashtagMaps, "./data/csv/" + sampleDir + "/" + solrCore);
		PredictionFileWriter.writeSimplePredictions(predictionValues, realValues, sampleDir + "/" + solrCore + "_solrht_normalized");
		return hashtagMaps;
	}
		
	public static String predictTrainSample(String sampleDir, String solrCore, String solrUrl, boolean hours, Integer recentTweetThreshold) {		
		List<Set<String>> predictionValues = new ArrayList<Set<String>>();
		List<Set<String>> realValues = new ArrayList<Set<String>>();
		SolrConnector trainConnector = new SolrConnector(solrUrl, solrCore + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, solrCore + "_test");
		
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
		
		PredictionFileWriter.writeSimplePredictions(predictionValues, realValues, sampleDir + "/" + solrCore + "_" + suffix);
		return suffix;
	}
	
	public static void printHashtagPrediction(Map<Integer, Map<Integer, Double>> predictions, String filePath) {
		try {
			FileWriter writer = new FileWriter(new File(filePath));
			BufferedWriter bw = new BufferedWriter(writer);
			
			for (Map.Entry<Integer, Map<Integer, Double>> predEntry : predictions.entrySet()) {
				bw.write(predEntry.getKey() + "|");
				int i = 1;
				for (Map.Entry<Integer, Double> mapEntry : predEntry.getValue().entrySet()) {
					bw.write(mapEntry.getKey() + ":" + mapEntry.getValue());
					if (i++ < predEntry.getValue().size()) {
						bw.write(";");
					}
				}
				bw.write("\n");
			}
			
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Map<Integer, Map<Integer, Double>> readHashtagPrediction(String filePath) {
		Map<Integer, Map<Integer, Double>> hashtagMaps = new LinkedHashMap<Integer, Map<Integer, Double>>();
		try {
			//FileReader reader = new FileReader(new File(filePath));
			InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF8");
			BufferedReader br = new BufferedReader(reader);
			
			String line = null;
			while((line = br.readLine()) != null) {
				Map<Integer, Double> tagMap = new LinkedHashMap<Integer, Double>();
				String[] parts = line.split("\\|");
				int userID = Integer.parseInt(parts[0]);
				if (parts.length > 1) {
					String[] tags = parts[1].split(";");
					for (String t : tags) {
						String[] tParts = t.split(":");
						if (tParts.length > 1 && !tParts[0].equals("null")) {
							try {
								tagMap.put(Integer.parseInt(tParts[0]), Double.parseDouble(tParts[1]));
							} catch (Exception e) {
								System.out.println("Parse Exception: " + tParts[0] + " " + tParts[1]);
							}
						}
					}
				}
				hashtagMaps.put(userID, tagMap);
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hashtagMaps;
	}
}
