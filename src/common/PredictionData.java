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
import java.util.List;
import java.util.Map;

public class PredictionData {
	
	private int userID;
	private List<String> realData;
	private List<String> predictionData;
	
	private double numFoundRelevantDocs;
	
	public PredictionData(int userID, List<String> realData, List<String> predictionData, int k) {
		this.userID = userID;
		this.realData = realData;
		if (k == 0) {
			this.predictionData = predictionData;
		} else if (predictionData.size() < k) {
			this.predictionData = new ArrayList<String>();
			this.predictionData.addAll(predictionData);
			//TODO: enable
			//while (this.predictionData.size() < k) {
			//	this.predictionData.add("x");
			//}
		} else {
			this.predictionData = predictionData.subList(0, k);
		}
		
		determineRelevantDocs();
 	}
	
	public double getRecall() {
		if (this.realData.size() != 0) {
			return this.numFoundRelevantDocs / this.realData.size();
		}
		return 0.0;
	}
	
	public double getPrecision() {
		if (this.predictionData.size() != 0) {
			return this.numFoundRelevantDocs / this.predictionData.size();
		}
		return 0.0;
	}
	
	public double getFMeasure() {
		if (getPrecision() + getRecall() != 0) {
			return 2.0 * ((getPrecision() * getRecall()) / (getPrecision() + getRecall()));
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
	
	private double getNovelty(int targetRes, List<Integer> resources, List<Map<Integer, Double>> resourceTopics) {
		double novelty = 0.0;
		
		int count = 0;
		Map<Integer, Double> targetTopics = resourceTopics.get(targetRes);
		for (int res : resources) {
			if (targetRes != res) {
				Map<Integer, Double> resTopics = resourceTopics.get(res);
				double disSim = 1.0 - Utilities.getCosineFloatSim(targetTopics, resTopics);
				novelty += disSim;
				count++;
			}
		}
		if (count == 0) {
			return 0.0;
		}
		return novelty / count;
	}
	
	public double getDiversity(List<Map<Integer, Double>> resourceTopics) {
		double diversity = 0.0;
		if (this.predictionData == null || this.predictionData.size() == 0) {
			return diversity;
		}
		
		List<Integer> predictionIDs = new ArrayList<Integer>(); 
		for (String res : this.predictionData) {
			predictionIDs.add(Integer.valueOf(res));
		}		
		for (int resID : predictionIDs) {
			diversity += getNovelty(resID, predictionIDs, resourceTopics);
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
			serendipity += getNovelty(resID, knownResources, resourceTopics);
		}		
		return serendipity / this.predictionData.size();
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
	
	public List<String> getRealData() {
		return this.realData;
	}
	
	public List<String> getPredictionData() {
		return this.predictionData;
	}
}
