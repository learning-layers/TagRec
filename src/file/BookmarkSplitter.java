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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.primitives.Ints;

import common.Bookmark;
import common.Utilities;
import file.preprocessing.CoreFiltering;

public class BookmarkSplitter {

	private BookmarkReader reader;

	public BookmarkSplitter(BookmarkReader reader) {
		this.reader = reader;
	}
	
	public List<Bookmark> getUserPercentage(int percentage, boolean usePercentage) {
		List<Bookmark> lines = new ArrayList<Bookmark>();
		int userSize = this.reader.getUsers().size();
		int userLimit = (usePercentage ? userSize * percentage / 100 : percentage);
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
		return lines;
	}
	
	// randomly gets x percentage of the user-profiles from the dataset
	public void splitUserPercentage(String filename, int percentage, boolean usePercentage, int count) {
		for (int i = 1; i <= count; i++) {
			List<Bookmark> lines = getUserPercentage(percentage, usePercentage);
			writeSample(this.reader, lines, filename + "_" + percentage + "_perc_" + i, null, false);
		}
	}
	
	// randomly splits the bookmarks
	public void splitFile(String filename, int testPercentage) {
		int testUserSize = this.reader.getBookmarks().size() * testPercentage / 100;
		int trainUserSize = this.reader.getBookmarks().size() - testUserSize;
		Collections.shuffle(this.reader.getBookmarks());
		List<Bookmark> userSample = this.reader.getBookmarks().subList(0, trainUserSize + testUserSize);
		
		List<Bookmark> trainUserSample = userSample.subList(0, trainUserSize);
		List<Bookmark> testUserSample = userSample.subList(trainUserSize, trainUserSize + testUserSize);
			
		writeSample(this.reader, trainUserSample, filename + "_train", null, false);
		writeSample(this.reader, testUserSample, filename + "_test", null, false);
		writeSample(this.reader, userSample, filename, null, false);
	}
	
	// puts the last bookmark of each user into the testset
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
				if (coldStart || userSize > 1) {
					testLines.add(data);
				} else {
					trainLines.add(data);
				}
			} else {
				trainLines.add(data);
			}
		}
		
		writeSample(this.reader, trainLines, filename + "_train", null, false);
		writeSample(this.reader, testLines, filename + "_test", null, false);
		trainLines.addAll(testLines);
		writeSample(this.reader, trainLines, filename, null, false);
	}
	
	// puts one bookmark at random of each user into the testset
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
		
		writeSample(this.reader, trainLines, filename + "_train", null, false);
		writeSample(this.reader, testLines, filename + "_test", null, false);
		trainLines.addAll(testLines);
		writeSample(this.reader, trainLines, filename, null, false);
	}
	
	public void leavePercentageOutSplit(String filename, int percentage, boolean last, Integer userNumber, boolean tagRec) {
		List<Bookmark> trainLines = new ArrayList<Bookmark>();
		List<Bookmark> testLines = new ArrayList<Bookmark>();
		Set<Integer> indices = new HashSet<Integer>();
		int currentUser = -1, userIndex = -1, userSize = -1;
		List<Bookmark> allLines = null;
		if (userNumber == null) {
			allLines = this.reader.getBookmarks();
		} else {
			allLines = getUserPercentage(userNumber, false);
		}
		
		for (int i = 0; i < allLines.size(); i++) {
			Bookmark data = allLines.get(i);
			if (currentUser != data.getUserID())  { // new user
				currentUser = data.getUserID();
				userSize = this.reader.getUserCounts().get(currentUser);			
				userIndex = 1;
				indices.clear();
				int limit = (int)((double)percentage / 100.0 * (double)userSize); // altenative: 10
				if (tagRec && limit == 0 && userSize > 1) {
					limit++;
				}
				while (indices.size() < limit) { // limit + 1 for cold-start users
					if (last) {
						indices.add(userSize - /*1 - */indices.size());
					} else {
						indices.add(1 + (int)(Math.random() * ((userSize/* - 1*/) + 1)));
					}
				}
			}
			if (indices.contains(userIndex++)) {
				testLines.add(data);
			} else {
				trainLines.add(data);
			}
		}
		
		Collections.sort(trainLines);
		Collections.sort(testLines);
		writeSample(this.reader, trainLines, filename + "_train", null, false);
		writeSample(this.reader, testLines, filename + "_test", null, false);
		trainLines.addAll(testLines);
		writeSample(this.reader, trainLines, filename, null, false);
	}
	
	// Statics -------------------------------------------------------------------------------------------------------------------------------------------
	
	public static boolean writeSample(BookmarkReader reader, List<Bookmark> userSample, String filename, List<int[]> catPredictions, boolean realValues) {
		try {
			FileWriter writer = new FileWriter(new File("./data/csv/" + filename + ".txt"));
			BufferedWriter bw = new BufferedWriter(writer);
			int userCount = 0;
			// TODO: check encoding
			for (Bookmark bookmark : userSample) {
				String user = (realValues ? reader.getUsers().get(bookmark.getUserID()).replace("\"", "") : Integer.toString(bookmark.getUserID()));
				String resource = (realValues ? reader.getResources().get(bookmark.getResourceID()).replace("\"", "") : Integer.toString(bookmark.getResourceID()));
				bw.write("\"" + user + "\";");
				bw.write("\"" + resource + "\";");
				bw.write("\"" + bookmark.getTimestamp().replace("\"", "") + "\";\"");
				int i = 0;
				for (int t : bookmark.getTags()) {
					String tag = (realValues ? reader.getTags().get(t).replace("\"", "") : Integer.toString(t));
					bw.write(tag);
					if (++i < bookmark.getTags().size()) {
						bw.write(',');
					}					
				}
				bw.write("\";\"");
				
				List<Integer> userCats = (catPredictions == null ? bookmark.getCategories() : Ints.asList(catPredictions.get(userCount++)));
				i = 0;
				for (int cat : userCats) {
					//bw.write(URLEncoder.encode((catPredictions == null ? reader.getCategories().get(cat).replace("\"", "") : reader.getTags().get(cat)).replace("\"", ""), "UTF-8"));
					bw.write("t" + cat);
					if (++i < userCats.size()) {
						bw.write(',');
					}					
				}
				bw.write("\"");
				if (bookmark.getRating() != -2) {
					bw.write(";\"" + (int)bookmark.getRating() + "\"");
				} else {
					bw.write(";\"\"");
				}
				if (bookmark.getTitle() != null) {
					bw.write(";\"" + bookmark.getTitle() + "\"");
				} /*else {
					bw.write(";\"\"");
				}*/
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
	
	public static void splitSample(String filename, String sampleName, int count, int percentage, boolean tagRec, boolean coldStart) {		
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);
		Collections.sort(reader.getBookmarks());
		BookmarkSplitter splitter = new BookmarkSplitter(reader);
		for (int i = 1; i <= count; i++) {
			if (percentage > 0) {
				splitter.leavePercentageOutSplit(sampleName, percentage, true, null, tagRec);
			} else {
				splitter.leaveLastOutSplit(sampleName, coldStart); // TODO check cold-start
			}
		}
	}
	
	public static void drawUserPercentageSample(String filename, int percentage) {
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);		
		BookmarkSplitter splitter = new BookmarkSplitter(reader);
		Collections.sort(reader.getBookmarks());
		splitter.splitUserPercentage(filename, percentage, true, 1);
	}
	
	public static void calculateCore(String filename, String sampleName, int userLevel, int resLevel, int tagLevel) {
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
				writeSample(reader, reader.getBookmarks(), coreResultfile, null, false);
				if (reader.getBookmarks().size() >= size) {
					return;
				}
				
				// re-read the filtered dataset			
				reader = new BookmarkReader(0, false);
				reader.readFile(coreResultfile);
				File file = new File("./data/csv/" + coreResultfile + ".txt");
				file.delete();
			}			
		}
	}
}
