package file.postprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;



import common.Bookmark;
import common.DoubleMapComparator;
import common.Utilities;

import file.BookmarkReader;

public class CatDescFiltering {

	private BookmarkReader reader;
	private List<Bookmark> trainList;
	private List<Map<Integer, Integer>> userMaps;
	private List<Set<Integer>> userResourceLists;
	private Map<Integer, Double> trrMap;
	private Map<Integer, Boolean> catDescMap;
	private double splitValue;
	private boolean describer;
	
	public CatDescFiltering(BookmarkReader reader, int trainSize) {
		this.reader = reader;
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		this.userMaps = Utilities.getUserMaps(this.trainList);
		this.userResourceLists = Utilities.getUserResourceLists(this.trainList);		
		this.trrMap = new LinkedHashMap<Integer, Double>();
		
		for (int i = 0; i < reader.getUsers().size(); i++) {
			//if (reader.getUserCounts().get(i) >= 20) {
 			double trr = this.getTRR(i);
			if (trr > 0.0) {
				this.trrMap.put(i, trr);
			}
			//}
		}
		Map<Integer, Double> sortedTrrMap = new TreeMap<Integer, Double>(new DoubleMapComparator(this.trrMap));
		sortedTrrMap.putAll(this.trrMap);
		
		int splitSize = this.trrMap.size() / 2; // reader.getUsers().size() / 2;
		System.out.println("Split size: " + splitSize);
		List<Double> trrList = new ArrayList<Double>(sortedTrrMap.values());
		this.splitValue = trrList.get(splitSize);
		System.out.println("TRR split value: " + this.splitValue);
		
		this.catDescMap = new LinkedHashMap<Integer, Boolean>();
		int i = 0;
		for (Map.Entry<Integer, Double> entry : sortedTrrMap.entrySet()) {
			if (i++ < splitSize) {
				this.catDescMap.put(entry.getKey(), true);
			} else {
				this.catDescMap.put(entry.getKey(), false);
			}
			//System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}
	
	public void setDescriber(boolean categorizer) {
		this.describer = categorizer;
	}
	
	public boolean getDescriber() {
		return this.describer;
	}
		
	public boolean evaluate(int userID) {
		Boolean describer = this.isDescriber(userID);
		if (describer != null) {
			return (describer.booleanValue() == this.describer);
		}
		return false;
	}
	
	// IMPORTANT: could be null! then do not evaluate!
	private Boolean isDescriber(int userID) {
		//return (this.trrMap.get(userID) > this.splitValue);
		if (this.catDescMap.containsKey(userID)) {
			return this.catDescMap.get(userID);
		}
		return null;
	}
	
	private double getTRR(int userID) {
		if (userID < this.userMaps.size() && userID < this.userResourceLists.size()) {
			double trr = (double)this.userMaps.get(userID).keySet().size() / (double)this.userResourceLists.get(userID).size();
			return trr;
		}
		return 0.0; // TODO: check if null should be returned
	}
	
	private double getTPP(int userID) {
		double tpp = Utilities.getMapCount(this.userMaps.get(userID)) / (double)this.userResourceLists.get(userID).size();
		return tpp;
	}
	
	private double getOrphanRatio(int userID) {
		Map<Integer, Integer> userMap = this.userMaps.get(userID);
		int n = (int)Math.ceil((double)Collections.max(userMap.values()) / 100.0);
		int count = 0;
		for (int val : userMap.values()) {
			if (val <= n) {
				count++;
			}
		}
		return (double)count / (double)userMap.size();
	}
	
	// Statics -----------------------------------------------------------------------------------------------------------------------
	
	public static CatDescFiltering instantiate(String filename, int trainSize) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		CatDescFiltering filter = new CatDescFiltering(reader, trainSize);
		
		/*
		int catCount = 0, descCount = 0;
		filter.setCategorizer(true);
		for (int i = 0; i < reader.getUsers().size(); i++) {
			try {
			if (filter.evaluate(i)) {
				catCount++;
			} else {
				descCount++;
			}
			} catch (Exception e) {
				// TODO: why?
			}
		}
		System.out.println("CatCount: " + catCount);
		System.out.println("DescCount: " + descCount);
		*/
		
		return filter;
	}
}
