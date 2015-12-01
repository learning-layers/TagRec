package processing;

import java.util.ArrayList;
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
	public static void predictSample(String sampleDir, String filename, String solrUrl) {		
		List<Set<String>> predictionValues = new ArrayList<Set<String>>();
		List<Set<String>> realValues = new ArrayList<Set<String>>();
		SolrConnector trainConnector = new SolrConnector(solrUrl, filename + "_train");
		SolrConnector testConnector = new SolrConnector(solrUrl, filename + "_test");
		
		Map<String, Set<String>> tweets = testConnector.getTweets();
		for (Map.Entry<String, Set<String>> tweet : tweets.entrySet()) {
			Map<String, Double> map = trainConnector.getTopHashtagsForTweetText(tweet.getKey(), LIMIT);
			predictionValues.add(map.keySet());
			realValues.add(tweet.getValue());
		}
		
		PredictionFileWriter.writeSimplePredictions(predictionValues, realValues, sampleDir + "/" + filename + "_solrht");
	}
}
