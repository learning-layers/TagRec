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
package processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import file.PredictionFileWriter;
import file.BookmarkReader;
import file.BookmarkSplitter;
import common.DoubleMapComparator;
import common.Bookmark;
import common.Utilities;

public class LanguageModelCalculator {

	private final static int REC_LIMIT = 10;
	
	private BookmarkReader reader;
	private double beta;
	private boolean userBased;
	private boolean resBased;
	
	private List<Map<Integer, Integer>> userMaps;
	private List<Double> userDenoms;
	private List<Map<Integer, Integer>> resMaps;
	private List<Double> resDenoms;
	
	public LanguageModelCalculator(BookmarkReader reader, int trainSize, int beta, boolean userBased, boolean resBased) {
		this.reader = reader;
		this.beta = (double)beta / 10.0;
		this.userBased = userBased;
		this.resBased = resBased;
		
		List<Bookmark> trainList = this.reader.getBookmarks().subList(0, trainSize);
		if (this.userBased) {
			this.userMaps = Utilities.getUserMaps(trainList);
			this.userDenoms = getDenoms(this.userMaps);
		}
		if (this.resBased) {
			this.resMaps = Utilities.getResMaps(trainList);
			this.resDenoms = getDenoms(this.resMaps);
		}
	}
	
	private List<Double> getDenoms(List<Map<Integer, Integer>> maps) {
		List<Double> denoms = new ArrayList<Double>();
		for (Map<Integer, Integer> map : maps) {
			double denom = 0.0;
			for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				denom += Math.pow(Math.E, entry.getValue());
			}
			denoms.add(denom);
		}
		
		return denoms;
	}
	
	// TODO: smoothing param
	public Map<Integer, Double> getRankedTagList(int userID, int resID, boolean sorting, boolean smoothing) {
		//double size = (double)this.reader.getTagAssignmentsCount();
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		if (this.userBased && this.userMaps != null && userID < this.userMaps.size()) {
			Map<Integer, Integer> userMap = this.userMaps.get(userID);
			for (Map.Entry<Integer, Integer> entry : userMap.entrySet()) {
				double userVal = this.beta * (Math.exp(entry.getValue().doubleValue()) / this.userDenoms.get(userID));
				resultMap.put(entry.getKey(), userVal);
			}
		}
		if (this.resBased && this.resMaps != null && resID < this.resMaps.size()) {
			Map<Integer, Integer> resMap = this.resMaps.get(resID);
			for (Map.Entry<Integer, Integer> entry : resMap.entrySet()) {
				double resVal = (1.0 - this.beta) * (Math.exp(entry.getValue().doubleValue()) / this.resDenoms.get(resID));
				Double val = resultMap.get(entry.getKey());
				resultMap.put(entry.getKey(), val == null ? resVal : val.doubleValue() + resVal);
			}
		}
				
		if (sorting) {
			Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			sortedResultMap.putAll(resultMap);
			
			Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>(REC_LIMIT);
			int i = 0;
			for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
				if (i++ < REC_LIMIT) {
					returnMap.put(entry.getKey(), entry.getValue());
				} else {
					break;
				}
			}
			return returnMap;
		}
		return resultMap;
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	private static String timeString = "";
	
	public static List<Map<Integer, Double>> startLanguageModelCreation(BookmarkReader reader, int sampleSize, boolean sorting, boolean userBased, boolean resBased, int beta, boolean smoothing) {
		timeString = "";
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		
		Stopwatch timer = new Stopwatch();
		timer.start();
		LanguageModelCalculator calculator = new LanguageModelCalculator(reader, trainSize, beta, userBased, resBased);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		if (trainSize == size) {
			trainSize = 0;
		}
		
		timer = new Stopwatch();
		timer.start();
		for (int i = trainSize; i < size; i++) { // the test-set
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID(), data.getWikiID(), sorting, smoothing);
			results.add(map);
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString += ("Full training time: " + trainingTime + "\n");
		timeString += ("Full test time: " + testTime + "\n");
		timeString += ("Average test time: " + testTime / (double)sampleSize) + "\n";
		timeString += ("Total time: " + (trainingTime + testTime) + "\n");
		return results;
	}
	
	public static void predictSample(String filename, int trainSize, int sampleSize, boolean userBased, boolean resBased, int beta) {
		//filename += "_res";

		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<Map<Integer, Double>> modelValues = startLanguageModelCreation(reader, sampleSize, true, userBased, resBased, beta, true);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < modelValues.size(); i++) {
			Map<Integer, Double> modelVal = modelValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}
		String suffix = "_mp_ur_";
		if (!userBased) {
			suffix = "_mp_r_";
		} else if (!resBased) {
			suffix = "_mp_u_";
		}
		reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		String outputFile = filename + suffix + beta;
		writer.writeFile(outputFile);
		
		Utilities.writeStringToFile("./data/metrics/" + outputFile + "_TIME.txt", timeString);
	}
}
