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

public class PredictionData {

	private List<String> realData;
	private List<String> predictionData;
	
	private double numFoundRelevantDocs;
	
	public PredictionData(List<String> realData, List<String> predictionData, int k) {
		this.realData = realData;
		if (k == 0 || predictionData.size() < k) {
			this.predictionData = predictionData;
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
	
	// Getter ------------------------------------------------------------------------------------------------
	
	public List<String> getRealData() {
		return this.realData;
	}
	
	public List<String> getPredictionData() {
		return this.predictionData;
	}
}
