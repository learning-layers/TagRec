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
import java.util.List;
import java.util.Map;

import common.Bookmark;

public class PredictionFileWriter {

	private static final int OUTPUT_LIMIT = 10;
	
	private BookmarkReader reader;
	private List<int[]> results;

	
	public PredictionFileWriter(BookmarkReader reader, List<int[]> results) {
		this.reader = reader;
		this.results = results;
	}
	
	public boolean writeFile(String filename) {		
		try {
			FileWriter writer = new FileWriter(new File("./data/results/" + filename + ".txt"));
			BufferedWriter bw = new BufferedWriter(writer);

			for (int i = 0; i < this.results.size(); i++) {
				int j = 0;
				String resultString = "";
				int[] userResults = this.results.get(i);
				Bookmark userData = this.reader.getBookmarks().get(i);
				List<Integer> userCats = userData.getTags();
				
				resultString += (userData.getUserID() + (userData.getWikiID() == -1 ? "" : "-" + userData.getWikiID()) + "|");
				for (int c : userCats) {
					//if (j++ < OUTPUT_LIMIT) {
						//resultString += (categories.get(c) + ", ");
						resultString += (c + ", ");
					//} else {
					//	break;
					//}
				}
				if (userCats.size() > 0) {
					resultString = resultString.substring(0, resultString.length() - 2);
				}
				resultString += "|";
				
				j = 0;
				for (int c : userResults) {
					if (j++ < OUTPUT_LIMIT) {
						resultString += (c + ", ");
					} else {
						break;
					}
				}
				if (userResults.length > 0) {
					resultString = resultString.substring(0, resultString.length() - 2);
				}
				resultString += "\n";
		
				bw.write(resultString);
			}
			
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean writeResourcePredictionsToFile(String filename, int trainSize, int neighborSize) {
		List<String> resourceList = this.reader.getResources();
		Map<Integer, List<Integer>> resourcesOfTestUsers = this.reader.getResourcesOfTestUsers(trainSize);
		
		try {
			FileWriter writer = new FileWriter(new File("./data/results/" + filename + ".txt"));
			BufferedWriter bw = new BufferedWriter(writer);
			
			int i=0;
			for (Integer userID : reader.getUniqueUserListFromTestSet(trainSize)) {
				String resultString = userID + "|";				
				String givenResourcesOfUser = "";
								
				for (Integer resourceID : resourcesOfTestUsers.get(userID)) {
					givenResourcesOfUser += resourceList.get(resourceID) + ", ";
				}				
				if (givenResourcesOfUser != "") {
					givenResourcesOfUser = givenResourcesOfUser.substring(0, givenResourcesOfUser.length() - 2);
				}				
				resultString += givenResourcesOfUser + "|";
								
				int[] recommendedResources = this.results.get(i);
				String recString = "";
				int cnt=0;

				for (int recResourceID : recommendedResources) {
				
					if (cnt++ < 20) {
						recString += resourceList.get(recResourceID) + ", ";
					} else {
						break;
					}
				}				
				if (!recString.equals("")) {
					recString = recString.substring(0, recString.length() - 2);
				}				
				resultString += recString + "\n";
				bw.write(resultString);
				i++;
			}
			
			bw.flush();
			bw.close();
			writer.close();
						
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
