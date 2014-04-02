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

import common.PredictionData;

public class PredictionFileReader {

	private List<PredictionData> predictions;
	private String filename;
	private int predictionCount;
	
	public PredictionFileReader() {
		this.predictions = new ArrayList<PredictionData>();
		this.predictionCount = 0;
	}
	
	public boolean readFile(String filename, int k) {
		try {
			this.filename = filename;
			FileReader reader = new FileReader(new File("./data/results/" + filename + ".txt"));
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\\|");
				String userID = lineParts[0];
				List<String> realData = Arrays.asList(lineParts[1].split(", "));
				if (lineParts.length > 2) {
					List<String> predictionData = Arrays.asList(lineParts[2].split(", "));
					if (predictionData.size() > 0) {
						PredictionData data = new PredictionData(realData, predictionData, k);
						this.predictions.add(data);
					} else {
						System.out.println("Line does not have predictions (inner)");
					}
				} else {
					System.out.println("Line does not have predictions (outer)");
				}
				this.predictionCount++;
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
