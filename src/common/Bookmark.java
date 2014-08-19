/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// TODO -----------------------------------------------------
// extend with fields and methods for title/url and description
public class Bookmark implements Comparable<Bookmark> {

	private int userID;
	private int resID;
	private String timestamp;
	private List<Integer> tags;
	
	private double rating;
	private List<Integer> categories;
	
	public Bookmark(int userID, int wikiID, String timestamp) {
		this.userID = userID;
		this.resID = wikiID;
		this.timestamp = timestamp;
		
		this.rating = -2.0;
		this.categories = new ArrayList<Integer>();
		this.tags = new ArrayList<Integer>();
	}
	
	@Override
	public int compareTo(Bookmark data) {
		//return (Long.parseLong(getTimestamp()) <= Long.parseLong(data.getTimestamp()) ? - 1 : 1);
		if (this.userID < data.getUserID()) {
			return -1;
		} else if (this.userID > data.userID) {
			return 1;
		} else {
			if (!this.timestamp.isEmpty() && !data.timestamp.isEmpty()) {
				if (Long.parseLong(this.timestamp) < Long.parseLong(data.timestamp)) { // < 
					return -1;
				} else if (Long.parseLong(this.timestamp) > Long.parseLong(data.timestamp)) { // >
					return 1;
				}
			}
		}
		return 0;
	}
	
	// Getter -------------------------------------------------------------------------
	
	public void setUserID(int userID) {
		this.userID = userID;
	}
	
	public int getUserID() {
		return this.userID;
	}
	
	public void setWikiID(int wikiID) {
		this.resID = wikiID;
	}
	
	public int getWikiID() {
		return this.resID;
	}
	
	public String getTimestamp() {
		return this.timestamp;
	}
	
	public double getRating() {
		return this.rating;
	}
	
	public void setRating(double rating) {
		this.rating = rating;
	}
	
	public List<Integer> getCategories() {	
		return this.categories;
	}
	
	public void setTags(List<Integer> tags) {
		this.tags = tags;
	}
	
	public List<Integer> getTags() {
		return this.tags;
	}
	
	// Statics ----------------------------------------------------------------------------------
	
	public static Bookmark getUserData(List<Bookmark> lines, int userID, int resID) {
		Bookmark returnData = null;
		for (Bookmark data : lines) {
			if (data.userID == userID) {
				returnData = data; // old
				if (resID != -1 && data.resID == resID) {
					returnData = data;
					return returnData;
				}
			}
		}
		return returnData;
	}
	
	public static Bookmark getResData(List<Bookmark> lines, int userID, int resID) {
		Bookmark returnData = null;
		for (Bookmark data : lines) {
			if (data.resID == resID) {
				returnData = data; // old
				if (userID != -1 && data.userID == userID) {
					returnData = data;
					return returnData;
				}
			}
		}
		return returnData;
	}
	
	public static Bookmark getLastData(List<Bookmark> lines, Set<Integer> ids) {
		long maxTimestamp = Long.MAX_VALUE;
		Bookmark returnData = null;
		for (Bookmark data : lines) {
			if (ids.contains(data.userID)) {
				long timestamp = Long.parseLong(data.timestamp);
				if (timestamp < maxTimestamp) {
					maxTimestamp = timestamp;
					returnData = data;
				}
			}
		}
		return returnData;
	}
	
	public static List<Integer> getResourcesFromUser(List<Bookmark> lines, int userID) {
		Set<Integer> resourceList = new HashSet<Integer>();		
		for (Bookmark data : lines) {		
			if (data.userID == userID) {
				resourceList.add(data.resID);
			}
		}		
		return new ArrayList<Integer>(resourceList);
	}
	

	public static Map<Integer, Double> getResourcesFromUserWithBLL(List<Bookmark> trainData, List<Bookmark> testData, int userID, List<Map<Integer, Double>> bllValues) {
		Map<Integer, Double> resourceMap = new LinkedHashMap<Integer, Double>();
		Map<Integer, Double> values = null;
		if (bllValues != null && userID < bllValues.size()) {
			values = bllValues.get(userID);
		}
		for (Bookmark data : trainData) {		
			if (data.userID == userID) {
				double val = 1.0;
				if (values != null) {
					val = 0.0;
					for (Integer t : data.getTags()) {
						Double v = values.get(t);
						if (v != null) {
							val += v.doubleValue();
						}
					}
				}
				resourceMap.put(data.resID, val);
			}
		}		
		return resourceMap;
	}
	
	public static Map<Integer, Double> getResourcesFromUserWithRec(List<Bookmark> trainData, List<Bookmark> testData, int userID, Double dValue, boolean sorting) {
		Map<Integer, Double> resourceMap = new LinkedHashMap<Integer, Double>();
		long refTimestamp = Utilities.getBaselineTimestamp(testData, userID, false);
		for (Bookmark data : trainData) {		
			if (data.userID == userID) {
				if (dValue != null) {
					long timestamp = Long.parseLong(data.getTimestamp());
					Double rec = Math.pow(refTimestamp - timestamp + 1.0, dValue.doubleValue() * (-1.0));
					if (!rec.isInfinite() && !rec.isNaN()) {
						//resourceMap.put(data.resID, Math.log(rec.doubleValue() + 1.0));
						resourceMap.put(data.resID, rec.doubleValue());
					} else {
						//System.out.println("BLL - NAN");
						//resourceMap.put(data.resID, Math.log(1.0 + 1.0));
						resourceMap.put(data.resID, 1.0);
					}
				} else {
					resourceMap.put(data.resID, 1.0);
				}
			}
		}
		
		if (sorting) {
			// return the sorted resources
			Map<Integer, Double> sortedRankedResources = new TreeMap<Integer, Double>(new DoubleMapComparator(resourceMap));
			sortedRankedResources.putAll(resourceMap);
			return sortedRankedResources;
		} else {
			return resourceMap;
		}
	}
}