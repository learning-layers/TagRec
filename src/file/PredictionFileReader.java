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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import common.PredictionData;
import common.Utilities;
import file.postprocessing.CatDescFiltering;

public class PredictionFileReader {

	private List<PredictionData> predictions;
	private String filename;
	private int predictionCount;
	
	public PredictionFileReader() {
		this.predictions = new ArrayList<PredictionData>();
		this.predictionCount = 0;
	}
	
	public boolean readFile(String filename, int k, BookmarkReader wikiReader, Integer minBookmarks, Integer maxBookmarks, Integer minResBookmarks, Integer maxResBookmarks, CatDescFiltering categorizer) {
		try {
			this.filename = filename;
			FileReader reader = new FileReader(new File("./data/results/" + filename + ".txt"));
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\\|");
				String[] parts = lineParts[0].split("-");
				int userID = -1;
				try {
					userID = Integer.parseInt(parts[0]);
				} catch (Exception e) {
					// string id - do nothing
				}
				int resID = -1;
				if (parts.length > 1) {
					resID = Integer.parseInt(parts[1]);
				}
				if (!Utilities.isEntityEvaluated(wikiReader, userID, minBookmarks, maxBookmarks, false) || !Utilities.isEntityEvaluated(wikiReader, resID, minResBookmarks, maxResBookmarks, true)) {
					continue; // skip this user if it shoudln't be evaluated - # bookmarks case
				}
				if (categorizer != null) {
					if (!categorizer.evaluate(userID)) {
						continue; // skip this user if it shoudln't be evaluated - categorizer case
					}
				}
				List<String> realData = Arrays.asList(lineParts[1].split(", "));
				if (lineParts.length > 2) {
					List<String> predictionData = Arrays.asList(lineParts[2].split(", "));
					if (predictionData.size() > 0) {
						PredictionData data = new PredictionData(userID, realData, predictionData, k);
						this.predictions.add(data);
						this.predictionCount++;
					} else {
						//System.out.println("Line does not have predictions (inner)");
						this.predictions.add(null);
					}
				} else {
					//System.out.println("Line does not have predictions (outer)");
					this.predictions.add(null);
				}
			}
			if (k == 20) {
				System.out.println("Number of users to predict: " + this.predictions.size());
			}
			br.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean readMyMediaLiteFile(String filename, int k, int trainSize, BookmarkReader bookmarkReader, Integer minBookmarks, Integer maxBookmarks, Integer minResBookmarks, Integer maxResBookmarks, CatDescFiltering categorizer) {
		try {
			this.filename = filename;
			List<Integer> testUsers = bookmarkReader.getUniqueUserListFromTestSet(trainSize);
			Map<Integer, List<Integer>> resourcesOfTestUsers = bookmarkReader.getResourcesOfTestUsers(trainSize);
			FileReader reader = new FileReader(new File("./data/results/" + filename + ".txt"));
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\\t");
				if (lineParts.length == 0) {
					continue; // skip invalid line
				}
				
				int userID = -1;
				try {
					userID = Integer.parseInt(lineParts[0]);
				} catch (Exception e) {
					continue; // skip user if userid is invalid
				}
				
				if (!testUsers.contains(userID)) {
					continue; // skip user if it is not part of the test-set
				}
				if (!Utilities.isEntityEvaluated(bookmarkReader, userID, minBookmarks, maxBookmarks, false)) {
					continue; // skip this user if it shoudln't be evaluated - # bookmarks case
				}
				if (categorizer != null) {
					if (!categorizer.evaluate(userID)) {
						continue; // skip this user if it shoudln't be evaluated - categorizer case
					}
				}
				List<Integer> testResources = resourcesOfTestUsers.get(userID);
				List<String> realData = new ArrayList<String>();
				for (int testRes : testResources) {
					realData.add(bookmarkReader.getResources().get(testRes));
				}
				
				if (lineParts.length > 1) {
					String recommendationString = lineParts[1].replace("[", "").replace("]", "");
					List<String> predictionStringData = Arrays.asList(recommendationString.split(","));
					if (predictionStringData.size() > 0) {
						List<String> predictionData = new ArrayList<String>();
						for (String predictionString : predictionStringData) {
							predictionData.add(predictionString.substring(0, predictionString.indexOf(":")));
						}
						PredictionData data = new PredictionData(userID, realData, predictionData, k);
						this.predictions.add(data);
						this.predictionCount++;
					} else {
						//System.out.println("Line does not have predictions (inner)");
						this.predictions.add(null);
					}
				} else {
					//System.out.println("Line does not have predictions (outer)");
					this.predictions.add(null);
				}
			}
			if (k == 1) {
				System.out.println("Number of users to predict: " + this.predictions.size());
			}
			br.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Getter ------------------------------------------------------------------------------------------------	
	public List<PredictionData> getPredictionData() {
		return this.predictions;
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public int getPredictionCount() {
		return this.predictionCount;
	}
}
