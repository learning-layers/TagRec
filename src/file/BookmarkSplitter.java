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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import common.IntMapComparator;
import common.Bookmark;
import common.Utilities;
import file.filtering.CoreFiltering;

public class BookmarkSplitter {

	private BookmarkReader reader;

	
	public BookmarkSplitter(BookmarkReader reader) {
		this.reader = reader;
	}
	
	public void splitFile(String filename, int testPercentage) {
		int testUserSize = this.reader.getBookmarks().size() * testPercentage / 100;
		int trainUserSize = this.reader.getBookmarks().size() - testUserSize;
		Collections.shuffle(this.reader.getBookmarks());
		List<Bookmark> userSample = this.reader.getBookmarks().subList(0, trainUserSize + testUserSize);
		
		//Collections.sort(userSample);
		
		List<Bookmark> trainUserSample = userSample.subList(0, trainUserSize);
		List<Bookmark> testUserSample = userSample.subList(trainUserSize, trainUserSize + testUserSize);
			
		writeWikiSample(this.reader, trainUserSample, filename + "_train", null);
		writeWikiSample(this.reader, testUserSample, filename + "_test", null);
		writeWikiSample(this.reader, userSample, filename, null);
	}
	
	public void splitUserPercentage(String filename, int percentage) {
		List<Bookmark> lines = new ArrayList<Bookmark>();
		int userSize = this.reader.getUsers().size();
		int userLimit = userSize * percentage / 100;
		List<Integer> randomIndices = Utilities.getRandomIndices(0, userSize - 1).subList(0, userLimit);
		int currentUser = -1;
		boolean takeUser = false;
		for (Bookmark data : this.reader.getBookmarks()) {
			if (currentUser != data.getUserID())  { // new user
				currentUser = data.getUserID();
				takeUser = randomIndices.contains(currentUser);
			}
			if (takeUser) {
				lines.add(data);
			}
		}
		
		writeWikiSample(this.reader, lines, filename + "_" + percentage + "_perc", null);
	}
	
	public void leaveLastOutSplit(String filename, boolean coldStart) {
		List<Bookmark> trainLines = new ArrayList<Bookmark>();
		List<Bookmark> testLines = new ArrayList<Bookmark>();
		int currentUser = -1, userIndex = 1, userSize = -1;
		for (Bookmark data : this.reader.getBookmarks()) {
			if (currentUser != data.getUserID())  { // new user
				currentUser = data.getUserID();
				userSize = this.reader.getUserCounts().get(currentUser);
				userIndex = 1;
			}
			if (userIndex++ == userSize) {
				if (coldStart || (!coldStart && userSize > 1)) {
					testLines.add(data);
				} else {
					trainLines.add(data);
				}
			} else {
				trainLines.add(data);
			}
		}
		
		writeWikiSample(this.reader, trainLines, filename + "_train", null);
		writeWikiSample(this.reader, testLines, filename + "_test", null);
		trainLines.addAll(testLines);
		writeWikiSample(this.reader, trainLines, filename, null);
	}
	
	public void leaveOneRandOutSplit(String filename) {
		List<Bookmark> trainLines = new ArrayList<Bookmark>();
		List<Bookmark> testLines = new ArrayList<Bookmark>();
		int currentUser = -1, userIndex = -1, index = -1, userSize = -1;
		for (Bookmark data : this.reader.getBookmarks()) {
			if (currentUser != data.getUserID())  { // new user
				currentUser = data.getUserID();
				userSize = this.reader.getUserCounts().get(currentUser);
				userIndex = 1;
				index = 1 + (int)(Math.random() * ((userSize - 1) + 1));
			}
			if (userIndex++ == index) {
				testLines.add(data);
			} else {
				trainLines.add(data);
			}
		}
		
		writeWikiSample(this.reader, trainLines, filename + "_train", null);
		writeWikiSample(this.reader, testLines, filename + "_test", null);
		trainLines.addAll(testLines);
		writeWikiSample(this.reader, trainLines, filename, null);
	}
	
	/**
	 * split bookmarks at given index 
	 * @param index
	 * @param filename
	 */
	public void leaveSomeOutSplit(int index, String filename) {
		List<Bookmark> trainLines = new ArrayList<Bookmark>();
		List<Bookmark> testLines = new ArrayList<Bookmark>();
		int currentUser = -1;
		int userIndex = 0;
		
		Collections.sort(this.reader.getBookmarks());

		int cntUsers=0;
		
		for (Bookmark data : this.reader.getBookmarks()) {
			if (currentUser != data.getUserID())  { // new user
				
				cntUsers++;
				/*if (cntUsers == 500) {
					break;
				}*/
				
				if (userIndex <= index && currentUser != -1) {
				}
				currentUser = data.getUserID();
				userIndex = 0;
			}
			if (++userIndex > index) {
				trainLines.add(data);
			} else {
				testLines.add(data);
			}
		}
		
		writeWikiSample(this.reader, trainLines, filename + "_train", null);
		writeWikiSample(this.reader, testLines, filename + "_test", null);
		trainLines.addAll(testLines);
		writeWikiSample(this.reader, trainLines, filename, null);
	}
	
	public void leavePercentageOutSplit(String filename, int percentage, boolean random) {
		List<Bookmark> trainLines = new ArrayList<Bookmark>();
		List<Bookmark> testLines = new ArrayList<Bookmark>();
		Set<Integer> indices = new HashSet<Integer>();
		int currentUser = -1, userIndex = -1, userSize = -1;
		for (int i = 0; i < this.reader.getBookmarks().size(); i++) {
			Bookmark data = this.reader.getBookmarks().get(i);
			if (currentUser != data.getUserID())  { // new user
				currentUser = data.getUserID();
				userSize = this.reader.getUserCounts().get(currentUser);
				userIndex = 1;
				indices.clear();
				int limit = (userSize - 1 < percentage ? userSize - 1 : percentage);
				if (random) {
					while (indices.size() < limit) {
						indices.add(1 + (int)(Math.random() * ((userSize - 1) + 1)));
					}
				} else {
					for (int index : getBestIndices(this.reader.getBookmarks().subList(i, i + userSize), false)) {
						if (indices.size() < limit) {
							indices.add(index);
						} else {
							break;
						}
					}
				}
			}
			if (indices.contains(userIndex++)) {
				testLines.add(data);
			} else {
				trainLines.add(data);
			}
		}
		
		writeWikiSample(this.reader, trainLines, filename + "_train", null);
		writeWikiSample(this.reader, testLines, filename + "_test", null);
		trainLines.addAll(testLines);
		writeWikiSample(this.reader, trainLines, filename, null);
	}
	
	private Set<Integer> getBestIndices(List<Bookmark> lines, boolean rating) {
		Map<Integer, Integer> countMap = new LinkedHashMap<Integer, Integer>();
		for (int i = 0; i < lines.size(); i++) {
			Bookmark data = lines.get(i);
			countMap.put(i + 1, rating ? (int)data.getRating() : this.reader.getResourceCounts().get(data.getWikiID()));
		}
		Map<Integer, Integer> sortedCountMap = new TreeMap<Integer, Integer>(new IntMapComparator(countMap));
		sortedCountMap.putAll(countMap);
		return sortedCountMap.keySet();
	}
	
	// Statics -------------------------------------------------------------------------------------------------------------------------------------------
	
	public static boolean writeWikiSample(BookmarkReader reader, List<Bookmark> userSample, String filename, List<int[]> catPredictions) {
		try {
			FileWriter writer = new FileWriter(new File("./data/csv/" + filename + ".txt"));
			BufferedWriter bw = new BufferedWriter(writer);
			int userCount = 0;
			// TODO: check encoding
			for (Bookmark bookmark : userSample) {
				bw.write("\"" + reader.getUsers().get(bookmark.getUserID()).replace("\"", "") + "\";");
				bw.write("\"" + reader.getResources().get(bookmark.getWikiID()).replace("\"", "") + "\";");
				bw.write("\"" + bookmark.getTimestamp().replace("\"", "") + "\";\"");
				int i = 0;
				for (int tag : bookmark.getTags()) {
					bw.write(URLEncoder.encode(reader.getTags().get(tag).replace("\"", ""), "UTF-8"));
					if (++i < bookmark.getTags().size()) {
						bw.write(',');
					}					
				}
				bw.write("\";\"");
				
				List<Integer> userCats = (catPredictions == null ? 
						bookmark.getCategories() : Ints.asList(catPredictions.get(userCount++)));
				i = 0;
				for (int cat : userCats) {
					bw.write(URLEncoder.encode((catPredictions == null ? reader.getCategories().get(cat).replace("\"", "") : reader.getTags().get(cat)).replace("\"", ""), "UTF-8"));
					if (++i < userCats.size()) {
						bw.write(',');
					}					
				}
				bw.write("\"");
				if (bookmark.getRating() != -2) {
					bw.write(";\"" + bookmark.getRating() + "\"");
				}
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static int determineMaxCore(String filename, String sampleName) {
		int core = 2;
		while(true) {
			int coreCount = splitSample(filename, sampleName, 1, core, core, core, false);
			core++;
			if (coreCount <= 0) {
				break;
			}
		}
		return core;
	}
	
	public static int splitSample(String filename, String sampleName, int count, int userLevel, int resLevel, int tagLevel, boolean resSplit) {
		
		String resultfile = sampleName + "_core_u" + userLevel + "_r" + resLevel + "_t" + tagLevel;
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);		
		
		System.out.println("Unique users before filtering: " + reader.getUsers().size());
		System.out.println("Unique resources before filtering: " + reader.getResources().size());
		System.out.println("Unique tags before filtering: " + reader.getTags().size());
		System.out.println("Lines before filtering: " + reader.getBookmarks().size());
		if (userLevel > 0 || resLevel > 0 || tagLevel > 0) {		
			int i = 0;
			while (true) {
				System.out.println("Core iteration: " + i);
				int size = reader.getBookmarks().size();
				CoreFiltering filtering = new CoreFiltering(reader);
				reader = filtering.filterOrphansIterative(userLevel, resLevel, tagLevel);
				String coreResultfile = resultfile + "_c" + ++i;
				writeWikiSample(reader, reader.getBookmarks(), coreResultfile, null);
				if (reader.getBookmarks().size() >= size) {
					return reader.getBookmarks().size();
				}
				
				// re-read the filtered dataset			
				reader = new BookmarkReader(0, false);
				reader.readFile(coreResultfile);
				File file = new File("./data/csv/" + coreResultfile + ".txt");
				file.delete();
			}			
		} else {		
			BookmarkSplitter splitter = new BookmarkSplitter(reader);
			// TODOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
			Collections.sort(reader.getBookmarks());
			for (int i = 1; i <= count; i++) {
				//splitter.splitFile(sampleName, 10);
				if (resSplit) {
					splitter.leavePercentageOutSplit(sampleName, 10, false);
				} else {
					splitter.leaveLastOutSplit(sampleName, true);
				}
				//splitter.splitUserPercentage(filename, 15);
			}
		}
		return -1;
	}
}
