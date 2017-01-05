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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import common.Bookmark;
import file.stemming.EnglishStemmer;

public class BookmarkReader {
	
	private final int countLimit;
	private List<Bookmark> userLines;
	private List<Bookmark> testLines;
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
	private EnglishStemmer stemmer;
	
	private boolean hasTimestamp = false;
	private long firstTimestamp = Long.MAX_VALUE;
	private long lastTimestamp = Long.MIN_VALUE;
	
	private Set<String> userResPairs = new HashSet<String>();
 	
	public BookmarkReader(int countLimit, boolean stemming) {
		this.countLimit = countLimit;
		this.userLines = new ArrayList<Bookmark>();
		this.testLines = null;
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
			this.stemmer = new EnglishStemmer();
		}
	}
	
	public boolean readFile(String filename) {	
		return doReadFile(null, filename);
	}
	
	public boolean readFile(String path, String filename) {
		return doReadFile(path, filename);
	}
	
	private boolean doReadFile(String path, String filename) {
		try {
			String filePath = "";
			if (path == null) {
				filePath = "./data/csv/" + filename + ".txt";
			} else {
				filePath = path + filename;
			}
			//FileReader reader = new FileReader(new File(filePath));
			InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF8");
			BufferedReader br = new BufferedReader(reader);
			List<String> categories = new ArrayList<String>(), tags = new ArrayList<String>();
			Bookmark userData = null;
			String userID = "", resID = "", timestamp = "";
			String[] lineParts = null;
			String line;
			
			while ((line = br.readLine()) != null) {
				lineParts = line.split("\";\"");
				if (lineParts.length < 4) {
					System.out.println("Line too short: " + this.userLines.size());
					continue;
				}
				//if (resID != "" && !resourceMap.containsKey(resID)) { // code for filtering tweets
					processUserData(userID, userData, tags, categories, resID);		
				//}
				// reset userdata
				userID = lineParts[0].replace("\"", "");
				resID = lineParts[1].replace("\"", "");
				timestamp = lineParts[2].replace("\"", "");
				userData = new Bookmark(-1, -1, timestamp);
				categories.clear();
				tags.clear();
				for (String tag : lineParts[3].replace("\"", "").split(",")) {
					String stemmedTag = tag.toLowerCase();
					if (!stemmedTag.isEmpty() && !tags.contains(stemmedTag)) {
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
							//if (cat.contains("_")) {
							//	categories.add(cat.substring(0, cat.indexOf("_")).toLowerCase());
							//} else {
								categories.add(cat.toLowerCase());
							//}
						}
					}
				}
				
				//if (lineParts.length > 5) { // is there a rating?
				//	try {
				//		userData.setRating(Double.parseDouble(lineParts[5].replace("\"", "")));
				//	} catch (Exception e) { /* do nothing */ }
				//}
				
				// TODO ----------------------
				// extend common/Bookmark class with fields for title (= lineParts[6]) and description (= lineParts[7])
				//if (lineParts.length > 6) { // is there a rating?
				//	try {
				//		userData.setTitle(lineParts[6].replace("\"", ""));
				//	} catch (Exception e) { /* do nothing */ }
				//}
			}
			processUserData(userID, userData, tags, categories, resID); // last user
			br.close();
			return true;
		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
		return false;
	}
	
	private void processUserData(String userID, Bookmark userData, List<String> tags, List<String> categories, String resID) {
		if (userID != "" /*&& tags.size() > 0 && !userData.getTimestamp().isEmpty()*/) {
			if (!userData.getTimestamp().isEmpty()) {
				if (!StringUtils.isNumeric(userData.getTimestamp())) {
					System.out.println("Invalid timestamp");
					return;
				}
				Long timestamp = userData.getTimestampAsLong();
				if (timestamp < this.firstTimestamp) {
					this.firstTimestamp = timestamp;
				} else if (timestamp > this.lastTimestamp) {
					this.lastTimestamp = timestamp;
				}
				this.hasTimestamp = true;
			}
			
			boolean doCount = (this.countLimit == 0 || this.userLines.size() < this.countLimit);
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
			Integer resIndex = this.resourceMap.get(resID);
			if (resIndex == null) {
				this.resources.add(resID);
				if (doCount) {
					this.resourceCounts.add(1);
				} else {
					this.resourceCounts.add(0);
				}
				resIndex = this.resources.size() - 1;
				this.resourceMap.put(resID, resIndex);
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
			//if (checkForDuplicate(userData)) {
			//	System.out.println("WARNING: Duplicate entry");
			//}
			this.userLines.add(userData);
			//if (this.userLines.size() % 100000 == 0) {
			//	System.out.println("Read in 10000000 lines");
			//}
		}
	}
	
	private boolean checkForDuplicate(Bookmark userData) {
		boolean dup = false;
		if (this.userResPairs.contains(userData.getUserID() + "_" + userData.getResourceID())) {
			dup = true;
		}
		this.userResPairs.add(userData.getUserID() + "_" + userData.getResourceID());
		return dup;
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
	
	public void setBookmarks(List<Bookmark> userLines) {
		this.userLines = userLines;
	}
	
	public List<Bookmark> getTestLines() {
		return this.testLines;
	}
	
	public void setTestLines(List<Bookmark> userLines) {
		this.testLines = userLines;
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
	
	public Map<String, Integer> getTagMap() {
		return this.tagMap;
	}
	
	public List<String> getResources() {
		return this.resources;
	}
	
	public Map<String, Integer> getResourceMap() {
		return this.resourceMap;
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
	
	public Map<String, Integer> getUserMap() {
		return this.userMap;
	}
	
	public int getCountLimit() {
		return this.countLimit;
	}
	
	public boolean hasTimestamp() {
		return this.hasTimestamp;
	}
	
	public Date getFirstTimestamp() {
		return new Date(this.firstTimestamp * 1000);
	}
	
	public Date getLastTimestamp() {
		return new Date(this.lastTimestamp * 1000);
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
			
			resources.add(data.getResourceID());
			
			resourcesMap.put(userID, resources);			
		}
		
		return resourcesMap;
	}
}