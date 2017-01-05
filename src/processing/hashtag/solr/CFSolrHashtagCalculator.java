package processing.hashtag.solr;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Ints;

import common.Bookmark;
import common.MapUtil;
import file.BookmarkReader;
import file.PredictionFileWriter;
import file.ResultSerializer;
import processing.CFTagRecommender;

public class CFSolrHashtagCalculator {
	
	public static void predictSample(String dir, String filename, int trainSize, double betaCB, String solrUrl, String solrCore) {
    	BookmarkReader reader = new BookmarkReader(0, false);
    	reader.readFile(filename);		
		Map<Integer, Map<Integer, Double>> contentBasedValues = null;
		if (solrUrl != null && solrCore != null) {   	
	    	if (new File("./data/results/" + dir + "/" + solrCore + "_cbpredictions.ser").exists()) {
		    	System.out.println("Found cb file ...");
		    	contentBasedValues = SolrHashtagCalculator.deSerializeHashtagPrediction("./data/results/" + dir + "/" + solrCore + "_cbpredictions.ser");
	    	} else {
	    		System.out.println("Did not find cb file ...");
	    		contentBasedValues = SolrHashtagCalculator.getNormalizedHashtagPredictions(dir, solrCore, solrUrl, reader, null);
	    	}
	    	System.out.println("Number of content-based recommendations: " + contentBasedValues.size());
	    }
		
        reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
        List<Bookmark> testLines = reader.getTestLines();
    	CFTagRecommender calculator = new CFTagRecommender(reader, trainSize, true, false, 5);
    	List<Map<Integer, Double>> resultValues = new ArrayList<Map<Integer, Double>>();
    	Map<Integer, Map<Integer, Double>> hashtagMaps = new LinkedHashMap<Integer, Map<Integer, Double>>();
        
        for (Bookmark b : testLines) {
        	// get cf Tags
        	if (contentBasedValues.containsKey(b.getUserID())) {
	        	Map<Integer, Double> cfTags = calculator.getRankedTagList(b.getUserID(), b.getResourceID(), false);
	        	MapUtil.normalizeMap(cfTags);
				if (cfTags != null && cfTags.entrySet() != null) {
					for (Map.Entry<Integer, Double> cfEntry : cfTags.entrySet()) {
						if (cfEntry != null && cfEntry.getKey() != null) {
							cfEntry.setValue(betaCB * cfEntry.getValue());
						}
					}
				}
	        	
	        	Map<Integer, Double> contentMap = contentBasedValues.get(b.getUserID());
				if (contentMap != null && contentMap.entrySet() != null) {
	    			for (Map.Entry<Integer, Double> contentEntry : contentMap.entrySet()) {
	    				if (contentEntry != null && contentEntry.getKey() != null) {
		    				Double cfVal = cfTags.get(contentEntry.getKey());
		    				double contentVal = (1.0 - betaCB) * contentEntry.getValue();
		    				cfTags.put(contentEntry.getKey(), cfVal == null ? contentVal : cfVal.doubleValue() + contentVal);
	    				}
	    			}
				}
				
				Map<Integer, Double> sortedMap = MapUtil.sortByValue(cfTags);
				resultValues.add(sortedMap);
				hashtagMaps.put(b.getUserID(), sortedMap);
        	} else {
        		// ignore all entries where no content-based recommendations where found
        		resultValues.add(null);
        	}
        }
        
        List<int[]> predictionValues = new ArrayList<int[]>();
        if (resultValues != null) {
	        for (int i = 0; i < resultValues.size(); i++) {
	            Map<Integer, Double> resultMap = resultValues.get(i);
	            if (resultMap != null && resultMap.keySet() != null) {
	            	predictionValues.add(Ints.toArray(resultMap.keySet()));
				} else {
					predictionValues.add(null);
				}
	        }
        }

        ResultSerializer.serializePredictions(hashtagMaps, "./data/results/" + dir + "/" + solrCore + "_cbcfpredictions.ser");
        PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
        writer.writeFile(filename + "_cf_cb_" + betaCB);
	}
}
