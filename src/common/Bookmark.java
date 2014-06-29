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
import java.util.List;
import java.util.Set;

public class Bookmark implements Comparable<Bookmark> {

	private int userID;
	private int resourceID;
	private String timestamp;
	private double rating;
	
	private List<Integer> categories;
	private List<Integer> tags;
	
	public Bookmark(int userID, int wikiID, String timestamp) {
		this.userID = userID;
		this.resourceID = wikiID;
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
				if (Long.parseLong(this.timestamp) < Long.parseLong(data.timestamp)) {
					return -1;
				} else if (Long.parseLong(this.timestamp) > Long.parseLong(data.timestamp)) {
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
		this.resourceID = wikiID;
	}
	
	public int getWikiID() {
		return this.resourceID;
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
				returnData = data;
				if (data.resourceID == resID) {
					return returnData;
				}
			}
		}
		return returnData;
	}
	
	public static Bookmark getResData(List<Bookmark> lines, int userID, int resID) {
		Bookmark returnData = null;
		for (Bookmark data : lines) {
			if (data.resourceID == resID) {
				returnData = data;
				if (data.userID == userID) {
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
				resourceList.add(data.resourceID);
			}
		}		
		return new ArrayList<Integer>(resourceList);
	}
}