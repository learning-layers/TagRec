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

package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import common.Bookmark;
import file.stemming.englishStemmer;

public class BookmarkReader {
	
	private final int countLimit;
	private List<Bookmark> userLines;
	private List<String> categories;
	
	private List<String> tags;
	private Map<String, Integer> tagMap;
	private List<Integer> tagCounts;
	private List<String> resources;
	private Map<String, Integer> resourceMap;
	private List<Integer> resourceCounts;
	private List<String> users;
	private Map<String, Integer> userMap;
	private List<Integer> userCounts;
	private englishStemmer stemmer;
	
	private boolean hasTimestamp = false;
 	
	public BookmarkReader(int countLimit, boolean stemming) {
		this.countLimit = countLimit;
		this.userLines = new ArrayList<Bookmark>();
		this.categories = new ArrayList<String>();
		
		this.tags = new ArrayList<String>();
		this.tagMap = new HashMap<String, Integer>();
		this.tagCounts = new ArrayList<Integer>();
		this.resources = new ArrayList<String>();
		this.resourceMap = new HashMap<String, Integer>();
		this.resourceCounts = new ArrayList<Integer>();
		this.users = new ArrayList<String>();
		this.userMap = new HashMap<String, Integer>();
		this.userCounts = new ArrayList<Integer>();
		if (stemming) {
			this.stemmer = new englishStemmer();
		}
	}
	
	public boolean readFile(String filename) {	
		try {
			FileReader reader = new FileReader(new File("./data/csv/" + filename + ".txt"));
			BufferedReader br = new BufferedReader(reader);
			List<String> categories = new ArrayList<String>(), tags = new ArrayList<String>();
			Bookmark userData = null;
			String userID = "", wikiID = "", timestamp = "";
			String[] lineParts = null;
			String line;
			
			while ((line = br.readLine()) != null) {
				lineParts = line.split("\";\"");
				if (lineParts.length < 4) {
					System.out.println("Line too short: " + this.userLines.size());
					continue;
				}
				processUserData(userID, userData, tags, categories, wikiID);			
				// reset userdata
				userID = lineParts[0].replace("\"", "");
				wikiID = lineParts[1].replace("\"", "");
				timestamp = lineParts[2].replace("\"", "");
				userData = new Bookmark(-1, -1, timestamp);
				categories.clear();
				tags.clear();
				for (String tag : lineParts[3].replace("\"", "").split(",")) {
					if (!tag.isEmpty()) {
						String stemmedTag = tag.toLowerCase();
						if (this.stemmer != null) {
							this.stemmer.setCurrent(stemmedTag);
							this.stemmer.stem();
							stemmedTag = this.stemmer.getCurrent();
						}
						tags.add(stemmedTag);
					}
				}
				if (lineParts.length > 4) { // are there categories
					for (String cat : lineParts[4].replace("\"", "").split(",")) {
						if (!cat.isEmpty()) {
							if (cat.contains("_")) {
								categories.add(cat.substring(0, cat.indexOf("_")).toLowerCase());
							} else {
								categories.add(cat.toLowerCase());
							}
						}
					}
				}
				if (lineParts.length > 5) { // is there a rating?
					try {
						userData.setRating(Double.parseDouble(lineParts[5].replace("\"", "")));
					} catch (Exception e) { /* do nothing */ }
				}
				
				// TODO ----------------------
				// extend common/Bookmark class with fields for title (= lineParts[6]) and description (= lineParts[7])
			}
			processUserData(userID, userData, tags, categories, wikiID); // last user
			br.close();
			return true;
		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
		return false;
	}
	
	private void processUserData(String userID, Bookmark userData, List<String> tags, List<String> categories, String wikiID) {
		if (userID != "" && tags.size() > 0/* && !userData.getTimestamp().isEmpty()*/) {
			if (!userData.getTimestamp().isEmpty()) {
				if (!StringUtils.isNumeric(userData.getTimestamp())) {
					System.out.println("Invalid timestamp");
					return;
				}
				this.hasTimestamp = true;
			}
			
			boolean doCount = (this.countLimit == 0 || this.userLines.size() < this.countLimit);
			//int userIndex = this.users.indexOf(userID);
			Integer userIndex = this.userMap.get(userID);
			if (userIndex == null) {
				this.users.add(userID);
				if (doCount) {
					this.userCounts.add(1);
				} else {
					this.userCounts.add(0);
				}
				userIndex = this.users.size() - 1;
				this.userMap.put(userID, userIndex);
			} else if (doCount) {
				this.userCounts.set(userIndex, this.userCounts.get(userIndex) + 1);
			}
			userData.setUserID(userIndex);
			//int resIndex = this.resources.indexOf(wikiID);
			Integer resIndex = this.resourceMap.get(wikiID);
			if (resIndex == null) {
				this.resources.add(wikiID);
				if (doCount) {
					this.resourceCounts.add(1);
				} else {
					this.resourceCounts.add(0);
				}
				resIndex = this.resources.size() - 1;
				this.resourceMap.put(wikiID, resIndex);
			} else if (doCount) {
				this.resourceCounts.set(resIndex, this.resourceCounts.get(resIndex) + 1);
			}
			userData.setWikiID(resIndex);
			
			for (String cat : categories) {
				int index = 0;
				if (!this.categories.contains(cat)) {
					this.categories.add(cat);
					index = this.categories.size() - 1;
				} else {
					index = this.categories.indexOf(cat);
				}
				userData.getCategories().add(index);
			}			
			for (String tag : tags) {
				//int tagIndex = this.tags.indexOf(tag);
				Integer tagIndex = this.tagMap.get(tag);
				if (tagIndex == null) { // new tag
					this.tags.add(tag);
					if (doCount) {
						this.tagCounts.add(1);
					} else {
						this.tagCounts.add(0);
					}
					tagIndex = this.tags.size() - 1;
					this.tagMap.put(tag, tagIndex);
				} else if (doCount) {
					this.tagCounts.set(tagIndex, this.tagCounts.get(tagIndex) + 1);
				}
				userData.getTags().add(tagIndex);
			}
			this.userLines.add(userData);
			//if (this.userLines.size() % 100000 == 0) {
			//	System.out.println("Read in 10000000 lines");
			//}
		}
	}
	
	// Getter + setter --------------------------------------------------------------------------------------------------------------------
	
	public int getTagAssignmentsCount() {
		int sum = 0;
		int count = 0;
		for (Bookmark data : this.userLines) {
			if (this.countLimit == 0 || count++ < this.countLimit) {
				sum += data.getTags().size();
			}
		}
		return sum;
	}
	
	public List<Bookmark> getBookmarks() {
		return this.userLines;
	}
	
	public void setUserLines(List<Bookmark> userLines) {
		this.userLines = userLines;
	}
	
	public List<String> getCategories() {
		return this.categories;
	}
	
	public List<String> getTags() {
		return this.tags;
	}
	
	public List<Integer> getTagCounts() {
		return this.tagCounts;
	}
	
	public List<String> getResources() {
		return this.resources;
	}
	
	public List<Integer> getResourceCounts() {
		return this.resourceCounts;
	}
	
	public List<String> getUsers() {
		return this.users;
	}
	
	public List<Integer> getUserCounts() {
		return this.userCounts;
	}
	
	public int getCountLimit() {
		return this.countLimit;
	}
	
	public boolean hasTimestamp() {
		return this.hasTimestamp;
	}
	
	public List<Integer> getUniqueUserListFromTestSet(int trainSize) {
		Set<Integer> userList = new HashSet<Integer>();	
		if (trainSize == -1) {
			trainSize = 0;
		}	
		for (int i = trainSize; i < this.userLines.size(); i++) {
			Bookmark data = getBookmarks().get(i);
			userList.add(data.getUserID());
		}
		
		List<Integer> result = new ArrayList<Integer>(userList);
		//Collections.sort(result);		
		return result;
	}
	
	public Map<Integer, List<Integer>> getResourcesOfTestUsers(int trainSize) {
		Map<Integer, List<Integer>> resourcesMap = new HashMap<Integer, List<Integer>>();
		
		if (trainSize == -1) {
			trainSize = 0;
		}
		
		for (int i = trainSize; i < getBookmarks().size(); i++) {
			Bookmark data = getBookmarks().get(i);
			int userID = data.getUserID();			
			List<Integer> resources = resourcesMap.get(userID);
			
			if (resources == null) {
				resources = new ArrayList<Integer>();
			}
			
			resources.add(data.getWikiID());
			
			resourcesMap.put(userID, resources);			
		}
		
		return resourcesMap;
	}
}