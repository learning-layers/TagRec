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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.UserData;
import common.Utilities;
import file.PredictionFileWriter;
import file.BookmarkReader;

public class ActCalculator {

	private final static int REC_LIMIT = 10;
	
	private BookmarkReader reader;
	private double dVal;
	private double beta;
	private boolean userBased;
	private boolean resBased;
	
	private List<Map<Integer, Double>> userMaps;
	private List<Double> userDenoms;
	private List<Long> userTimestamps;
	private List<Map<Integer, Double>> resMaps;
	private List<Double> resDenoms;
	private List<Long> resTimestamps;
	
	private BM25Calculator cfCalc;
	
	public ActCalculator(BookmarkReader reader, int trainSize, int dVal, int beta, boolean userBased, boolean resBased) {
		this.reader = reader;
		this.dVal = (double)dVal / 10.0;
		this.beta = (double)beta / 10.0;
		this.userBased = userBased;
		this.resBased = resBased;
		
		List<UserData> trainList = this.reader.getUserLines().subList(0, trainSize);
		List<UserData> testList = this.reader.getUserLines().subList(trainSize, reader.getUserLines().size());
		
		this.userDenoms = new ArrayList<Double>();
		this.userTimestamps = new ArrayList<Long>();
		if (this.userBased) {
			this.userMaps = getArtifactMaps(trainList, testList, false, this.userTimestamps, this.userDenoms, this.dVal, false);
		}
		this.resDenoms = new ArrayList<Double>();
		this.resTimestamps = new ArrayList<Long>();
		if (this.resBased) {
			this.resMaps = getArtifactMaps(trainList, testList, true, this.resTimestamps, this.resDenoms, this.dVal, false);
			//this.resMaps = Utilities.getRelativeMaps(trainList, true);
			//this.cfCalc = new BM25Calculator(this.reader, trainSize, true, false, true, 5);
		}
	}
	
	public Map<Integer, Double> getRankedTagList(int userID, int resID, boolean sorting) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		if (this.userBased && this.userMaps != null && userID < this.userMaps.size()) {
			Map<Integer, Double> userMap = this.userMaps.get(userID);
			for (Map.Entry<Integer, Double> entry : userMap.entrySet()) {
				double userVal = this.beta * (Math.exp(entry.getValue().doubleValue()) / this.userDenoms.get(userID));
				resultMap.put(entry.getKey(), userVal);
			}
		}
		if (this.resBased && this.resMaps != null && resID < this.resMaps.size()) {
			Map<Integer, Double> resMap = this.resMaps.get(resID);
			for (Map.Entry<Integer, Double> entry : resMap.entrySet()) {
				double resVal = (1.0 - this.beta) * (Math.exp(entry.getValue().doubleValue()) / this.resDenoms.get(resID));
				Double val = resultMap.get(entry.getKey());
				resultMap.put(entry.getKey(), val == null ? resVal : val.doubleValue() + resVal);
			}
		}
		
		/*if (resultMap.size() == 0) {
			double i = 10.0;
			for (int tag : BaselineCalculator.getPopularTagList(this.reader, 10)) {
				resultMap.put(tag, i--);
			}
		}
		*/
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
		
	public static List<Map<Integer, Double>> getArtifactMaps(List<UserData> userLines, List<UserData> testLines, boolean resource,
			List<Long> timestampList, List<Double> denomList, double dVal, boolean normalize) {
		
		List<Map<Integer, Double>> maps = new ArrayList<Map<Integer, Double>>();
		for (UserData data : userLines) {
			int refID = 0;
			if (resource) {
				refID = data.getWikiID();
			} else {
				refID = data.getUserID();
			}
			long baselineTimestamp = -1;
			if (refID >= maps.size()) {
				if (resource) {
					//refIDs = Utilities.getUsersByResource(userLines, data.getWikiID());
					baselineTimestamp = 1;
				} else {
					baselineTimestamp = Utilities.getBaselineTimestamp(testLines, refID);
				}
				timestampList.add(baselineTimestamp);
				if (baselineTimestamp != -1) {
					maps.add(addActValue(data, new LinkedHashMap<Integer, Double>(), baselineTimestamp, resource, dVal));
				}
			} else {
				baselineTimestamp = timestampList.get(refID);
				if (baselineTimestamp != -1) {
					addActValue(data, maps.get(refID), baselineTimestamp, resource, dVal);
				}
			}
		}
		// normalize values
		for (Map<Integer, Double> map : maps) {
			double denom = 0.0;
			for (Map.Entry<Integer, Double> entry : map.entrySet()) {
				double actVal = Math.log(entry.getValue());
				denom += Math.exp(actVal);
				entry.setValue(actVal);
			}
			denomList.add(denom);
			if (normalize) {
				for (Map.Entry<Integer, Double> entry : map.entrySet()) {
					double actVal = Math.exp(entry.getValue());
					entry.setValue(actVal / denom);
				}
			}
		}
		
		return maps;
	}
	
	private static Map<Integer, Double> addActValue(UserData data, Map<Integer, Double> actValues, long baselineTimestamp, boolean resource, double dVal) {
		if (!data.getTimestamp().isEmpty()) {
			Double newAct = 0.0;
			if (resource) {
				newAct = 1.0;
			} else {
				Double recency = (double)(baselineTimestamp - Long.parseLong(data.getTimestamp()) + 1.0);
				//double recency = Math.ceil((baselineTimestamp - Long.parseLong(data.getTimestamp()) + 1.0) / 60.0 / 60.0 / 24.0 / 365.0 / 10);
				//System.out.println(recency);			
				newAct = Math.pow(recency, dVal * -1.0);
			}
			for (Integer value : data.getTags()) {
				Double oldAct = actValues.get(value);
				if (!newAct.isInfinite() && !newAct.isNaN()) {
					actValues.put(value, (oldAct != null ? oldAct + newAct : newAct));
				} else {
					System.out.println(data.getWikiID() + "_" + baselineTimestamp + " " + data.getTimestamp());
				}
			}
		}
		return actValues;
	}
	
	// Helpers -------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	private static String timeString;
	
	private static List<Map<Integer, Double>> startActCreation(BookmarkReader reader, int sampleSize, boolean sorting, boolean userBased, boolean resBased, int dVal, int beta) {
		timeString = "";
		int size = reader.getUserLines().size();
		int trainSize = size - sampleSize;
		
		Stopwatch timer = new Stopwatch();
		timer.start();
		ActCalculator calculator = new ActCalculator(reader, trainSize, dVal, beta, userBased, resBased);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		if (trainSize == size) {
			trainSize = 0;
		}
		
		timer = new Stopwatch();
		timer.start();
		for (int i = trainSize; i < size; i++) { // the test-set
			UserData data = reader.getUserLines().get(i);
			Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID(), data.getWikiID(), sorting);
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
	
	public static void predictSample(String filename, int trainSize, int sampleSize, boolean userBased, boolean resBased, int dVal, int beta) {
		//filename += "_res";

		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<Map<Integer, Double>> actValues = startActCreation(reader, sampleSize, true, userBased, resBased, dVal, beta);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < actValues.size(); i++) {
			Map<Integer, Double> modelVal = actValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}
		String suffix = "_bll_c_";
		if (!userBased) {
			suffix = "_bll_r_";
		} else if (!resBased) {
			suffix = "_bll_";
		}
		reader.setUserLines(reader.getUserLines().subList(trainSize, reader.getUserLines().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		String outputfile = filename + suffix + beta + "_" + dVal;
		writer.writeFile(outputfile);
		
		Utilities.writeStringToFile("./data/metrics/" + outputfile + "_TIME.txt", timeString);
	}
}
