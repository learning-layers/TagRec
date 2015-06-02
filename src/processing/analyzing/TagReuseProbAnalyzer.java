package processing.analyzing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.Bookmark;
import common.CooccurenceMatrix;
import common.Utilities;
import file.BookmarkReader;

public class TagReuseProbAnalyzer {

	private BookmarkReader reader = null;
	private List<Bookmark> trainSet = null;
	private List<Bookmark> testSet = null;
	private List<List<Bookmark>> userBookmarks = null;
	private List<UserTagProperties> userTagProperties = null;
	private CooccurenceMatrix tagMatrix = null;
	
	public TagReuseProbAnalyzer(BookmarkReader reader, int trainSize, boolean context) {
		this.reader = reader;
		this.trainSet = reader.getBookmarks().subList(0, trainSize);
		this.testSet = reader.getBookmarks().subList(trainSize, reader.getBookmarks().size());
		this.userTagProperties = new ArrayList<UserTagProperties>();
		if (context) {
			this.tagMatrix = new CooccurenceMatrix(this.trainSet, reader.getTagCounts(), false);
		}
		
		this.userBookmarks = Utilities.getBookmarks(this.trainSet, false);
		int userID = 0;
		for (List<Bookmark> currentBookmarks : this.userBookmarks) {
			if (userID != currentBookmarks.get(0).getUserID()) {
				System.out.println("ERROR");
			}
			Bookmark testBookmark = Bookmark.getBookmark(this.testSet, userID, -1);
			if (testBookmark != null) {
				this.userTagProperties.add(new UserTagProperties(currentBookmarks, testBookmark, this.tagMatrix));
			}
			userID++;
		}
	}
	
	public void mergeAndWriteUserTagProperties(String filename) {
		Map<Integer, ReuseProbValue> tagFrequencies = new LinkedHashMap<Integer, ReuseProbValue>();
		Map<Integer, ReuseProbValue> tagRecencies = new LinkedHashMap<Integer, ReuseProbValue>();
		Map<Integer, ReuseProbValue> tagContextSim = new LinkedHashMap<Integer, ReuseProbValue>();
		
		for (UserTagProperties tagProperties : this.userTagProperties) {
			for (Map.Entry<Integer, Integer> tagFrequ : tagProperties.getTagCounts().entrySet()) {
				ReuseProbValue value = tagFrequencies.get(tagFrequ.getValue());
				if (value == null) {
					value = new ReuseProbValue();
					tagFrequencies.put(tagFrequ.getValue(), value);
				}
				value.increment(tagProperties.getReuseProb().get(tagFrequ.getKey()));
			}
			
			for (Map.Entry<Integer, Integer> tagRec : tagProperties.getTagRecencies().entrySet()) {
				if (tagRec.getValue() > 0) {
					ReuseProbValue value = tagRecencies.get(tagRec.getValue());
					if (value == null) {
						value = new ReuseProbValue();
						tagRecencies.put(tagRec.getValue(), value);
					}
					value.increment(tagProperties.getReuseProb().get(tagRec.getKey()));
				}
			}
			
			if (this.tagMatrix != null) {
				for (Map.Entry<Integer, Integer> tagSim : tagProperties.getTagContextSim().entrySet()) {
					ReuseProbValue value = tagContextSim.get(tagSim.getValue());
					if (value == null) {
						value = new ReuseProbValue();
						tagContextSim.put(tagSim.getValue(), value);
					}
					value.increment(tagProperties.getReuseProb().get(tagSim.getKey()));
				}
			}
		}
		
		// sort and write
		Map<Integer, ReuseProbValue> sortedTagFrequencies = new TreeMap<Integer, ReuseProbValue>(tagFrequencies);
		Map<Integer, ReuseProbValue> sortedTagRecencies = new TreeMap<Integer, ReuseProbValue>(tagRecencies);		
		writeMap(filename + "_Frequency", sortedTagFrequencies, false, false);
		writeMap(filename + "_Recency", sortedTagRecencies, true, false);
		writeMap(filename + "_Recency_power", sortedTagRecencies, true, true);
		if (this.tagMatrix != null) {
			Map<Integer, ReuseProbValue> sortedTagContextSim = new TreeMap<Integer, ReuseProbValue>(tagContextSim);
			writeMap(filename + "_ContextSim", sortedTagContextSim, false, false);
		}
	}
	
	private void writeMap(String filename, Map<Integer, ReuseProbValue> map, boolean normalize, boolean powerlaw) {
		try {
			FileWriter writer = new FileWriter(new File("./data/csv/" + filename + ".txt"));
			BufferedWriter bw = new BufferedWriter(writer);
			double normVal = -1;
			if (normalize) {
				normVal = getMapNormalizeValue(map);
			}
			for (Map.Entry<Integer, ReuseProbValue> entry : map.entrySet()) {
				double sum = entry.getValue().getSum();
				double count = entry.getValue().getCount();
				if (sum > 0.0) {
					if (normalize) {
						if (powerlaw) {
							for (int i = 1; i < sum; i++) {
								bw.write(entry.getKey() + "\n");	
							}
						} else {
							bw.write(entry.getKey() + ";" + sum / normVal + "\n");
						}
					} else {
						bw.write(entry.getKey() + ";" + sum / count + "\n");
					}
				}
			}		
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double getMapNormalizeValue(Map<Integer, ReuseProbValue> map) {
		double sum = 0.0;
		for (Map.Entry<Integer, ReuseProbValue> entry : map.entrySet()) {
			if (entry.getValue().getSum() > sum) {
				//sum = entry.getValue().getSum();
				if (entry.getValue().getSum() > sum) {
					sum = entry.getValue().getSum();
				}
			}
		}
		return sum;
	}
	
	// Statics
	public static void analyzeSample(String filename, int trainSize, int sampleSize, boolean context) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		TagReuseProbAnalyzer analyzer = new TagReuseProbAnalyzer(reader, trainSize, context);
		analyzer.mergeAndWriteUserTagProperties(filename);
	}
}
