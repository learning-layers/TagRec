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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PredictionData {
	
	private int userID;
	private int resID;
	private int k;
	private List<String> realData = new ArrayList<String>();
	private List<String> predictionData = new ArrayList<String>();
	
	private double numFoundRelevantDocs;
	
	public PredictionData(int userID, int resID, List<String> realData, List<String> predictionData, int k) {
		this.userID = userID;
		this.resID = resID;
		for (String rd : realData) {
			this.realData.add(rd.toLowerCase());
		}
		this.k = k;
		for (String pd : predictionData) {
			if (k == 0 || this.predictionData.size() < k) {
				this.predictionData.add(pd.toLowerCase());
			}
		}
		
		determineRelevantDocs();
 	}
	
	public double getRecall() {
		if (this.realData.size() != 0) {
			return this.numFoundRelevantDocs / this.realData.size();
		}
		return 0.0;
	}
	
	public double getPrecision(boolean recommTags) {
		if (this.predictionData.size() != 0) {
			//return this.numFoundRelevantDocs / (recommTags ? this.predictionData.size() : this.k);
			//TODO: use this.k
			return this.numFoundRelevantDocs / this.k;
		}
		return 0.0;
	}
	
	public double getFMeasure(boolean recommTags) {
		if (getPrecision(recommTags) + getRecall() != 0) {
			return 2.0 * ((getPrecision(recommTags) * getRecall()) / (getPrecision(recommTags) + getRecall()));
		}
		return 0.0;
	}
	
	public double getMRR() {
		if (this.predictionData.size() != 0 && this.realData.size() != 0) {
			double sum = 0.0;
			for (String val : this.realData) {
				int index = 0;
				if ((index = this.predictionData.indexOf(val)) != -1) {
					sum += (1.0 / (index + 1));
				} else {
					sum += 0.0;
				}
			}
			return sum / this.realData.size(); // TODO: correct?
		}
		return 0.0;
	}
	
	public double getMAP() {
		if (this.predictionData.size() != 0 && this.realData.size() != 0 && this.numFoundRelevantDocs != 0) {
			double sum = 0.0;
			for (int i = 1; i <= this.predictionData.size(); i++) {
				sum += (getPrecisionK(i) * isCorrect(i - 1));
			}
			return sum / this.realData.size();
		}
		return 0.0;
	}
	
	public double getCoverage() {
		return (this.predictionData.size() > 0 ? 1.0 : 0.0);
	}
	
	private double getPrecisionK(int k) {
		if (k != 0 && k <= this.predictionData.size()) {
			List<String> foundRelevantDocs = new ArrayList<String>(this.realData);
			foundRelevantDocs.retainAll(this.predictionData.subList(0, k));
			double numFoundRelevantDocs = foundRelevantDocs.size();
			return numFoundRelevantDocs / k;
		}
		return 0.0;
	}
	
	private double isCorrect(int n) {
		if (this.predictionData.size() > n && this.realData.contains(this.predictionData.get(n))) {
			return 1.0;
		}
		return 0.0;
	}
	
	private void determineRelevantDocs() {
		List<String> foundRelevantDocs = new ArrayList<String>(this.realData);
		foundRelevantDocs.retainAll(this.predictionData);
		this.numFoundRelevantDocs = foundRelevantDocs.size();
	}
	
	// Resource-rec metrics	
	private double getNovelty(int targetRes, List<Integer> resources, List<Map<Integer, Double>> resourceTopics, boolean cosine) {
		double novelty = 0.0;
		
		int count = 0;
		Map<Integer, Double> targetTopics = resourceTopics.get(targetRes);
		for (int res : resources) {
			if (targetRes != res) {
				Map<Integer, Double> resTopics = resourceTopics.get(res);
				double sim = (cosine ? Utilities.getCosineFloatSim(targetTopics, resTopics) : Utilities.getJaccardFloatSim(targetTopics, resTopics));
				double disSim = 1.0 - sim;
				novelty += disSim;
				count++;
			}
		}
		if (count == 0) {
			return 0.0;
		}
		return novelty / count;
	}
	
	public double getDiversity(List<Map<Integer, Double>> resourceTopics, boolean cosine) {
		double diversity = 0.0;
		if (this.predictionData == null || this.predictionData.size() == 0) {
			return diversity;
		}
		
		List<Integer> predictionIDs = new ArrayList<Integer>(); 
		for (String res : this.predictionData) {
			predictionIDs.add(Integer.valueOf(res));
		}		
		for (int resID : predictionIDs) {
			diversity += getNovelty(resID, predictionIDs, resourceTopics, cosine);
		}		
		return diversity / this.predictionData.size();
	}
	
	public double getSerendipity(List<Map<Integer, Double>> resourceTopics, List<Integer> knownResources) {
		double serendipity = 0.0;
		if (this.predictionData == null || this.predictionData.size() == 0) {
			return serendipity;
		}
		if (knownResources == null || knownResources.size() == 0) {
			return 1.0;
		}
		
		for (String res : this.predictionData) {
			int resID = Integer.parseInt(res);
			serendipity += getNovelty(resID, knownResources, resourceTopics, true);
		}		
		return serendipity / this.predictionData.size();
	}
	
	public double getTagDiversity(List<Map<Integer, Double>> tagEntities) {
		double diversity = 0.0;
		if (this.predictionData == null || this.predictionData.size() == 0) {
			return diversity;
		}
		
		List<String> predictionIDs = new ArrayList<String>(); 
		for (String res : this.predictionData) {
			//predictionIDs.add(Integer.valueOf(res));
			predictionIDs.add(res);
		}
		int k = predictionIDs.size();
		for (int i = 0; i < k; i++) {
			Map<Integer, Double> targetEntities = tagEntities.get(i);
			for (int j = i + 1; j < k; j++) {
				Map<Integer, Double> sourceEntities = tagEntities.get(j);
				diversity += (1.0 - Utilities.getJaccardFloatSim(targetEntities, sourceEntities));
			}
		}
		double normConstant = (k * k - k) / 2.0;
		if (normConstant > 0.0) {
			diversity /= normConstant;
		}
		return diversity;
	}
	
	public double getTagSerendipity(Map<Integer, Integer> tagFrequencyMap, boolean cosine) {
		Double serendipity = 0.0;
		if (this.predictionData == null || this.predictionData.size() == 0) {
			return 0.0;
		}
		if (tagFrequencyMap == null || tagFrequencyMap.size() == 0) {
			return 1.0;
		}
		if (!cosine) {
			double i = 1.0;
			double maxIFF = Double.MIN_VALUE;
			for (String tag : this.predictionData) {
				int tagID = Integer.parseInt(tag);
				Integer tagCount = tagFrequencyMap.get(tagID);
				double iff = Math.log(((double)tagFrequencyMap.size() + 1.0) / ((tagCount == null ? 0.0 : tagCount.doubleValue()) + 1.0));
				double disc = 1.0 / Math.log(1.0 + i++);
				serendipity += (disc * iff);
				if (iff > maxIFF) {
					maxIFF = iff;
				}
			}
			double normConstant = 0.0;
			for (double j = 1.0; j <= this.predictionData.size(); j++) {
				normConstant += ((1.0 / Math.log(1.0 + j)) * maxIFF);
			}
			if (normConstant > 0.0) {
				serendipity /= normConstant;
			}
			if (serendipity.isInfinite() || serendipity.isNaN()) {
				serendipity = 1.0;
			}
		} else {
			serendipity = (1.0 - Utilities.getCosineSim(getPredictionDataAsMap(), tagFrequencyMap));
		}
		return serendipity;
	}
	
	/**
	 * Compute the normalized discounted cumulative gain (NDCG) of a list of ranked items.
	 *
	 * @return the NDCG for the given data
	 */
	public double getNDCG() {
		double dcg = 0;
		double idcg = calculateIDCG(this.realData.size());

		if (idcg == 0) {
			return 0;
		}

		for (int i = 0; i < this.predictionData.size(); i++) {
			String predictedItem = this.predictionData.get(i);

			int itemRelevance = 1;
			if (!this.realData.contains(predictedItem))
				itemRelevance = 0;

			// compute NDCG part
			// the relevance in the DCG part is either 1 (the item is contained in real data) 
			// or 0 (item is not contained in the real data)
			int rank = i + 1;
			
			dcg += (Math.pow(2, itemRelevance) - 1.0) * (Math.log(2) / Math.log(rank + 1));
		}

		return dcg / idcg;
	}
	
	/**
	 * Calculates the iDCG (ideal relevant item ranking)
	 * 
	 * @param n size of the expected resource list
	 * @return iDCG
	 */
	public double calculateIDCG(int n) {
		double idcg = 0;

		for (int i = 0; i < n; i++){
			// if can get relevance for every item should replace the relevance score at this point, else
			// every item in the ideal case has relevance of 1
			int itemRelevance = 1;
			idcg += (Math.pow(2, itemRelevance) - 1.0) * ( Math.log(2) / Math.log(i + 2) );
		}

		return idcg;
	}
	
	
	// Getter ------------------------------------------------------------------------------------------------
	
	public int getUserID() {
		return this.userID;
	}
	
	public int getResID() {
		return this.resID;
	}
	
	public List<String> getRealData() {
		return this.realData;
	}
	
	public List<String> getPredictionData() {
		return this.predictionData;
	}
	
	public Map<Integer, Integer> getPredictionDataAsMap() {
		Map<Integer, Integer> returnMap = new LinkedHashMap<Integer, Integer>();
		for (String data : this.predictionData) {
			int intVal = Integer.parseInt(data);
			returnMap.put(intVal, 1);
		}
		return returnMap;
	}
}
