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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import common.Bookmark;
import common.PredictionData;
import common.Utilities;

import file.PredictionFileReader;
import file.BookmarkReader;

public class MetricsCalculator {

	private PredictionFileReader reader;	
	private double recall;
	private double precision;
	private double fMeasure;
	private double mrr;
	private double map;
	private double userCoverage;
	private double diversity;
	private double serendipity;
	private double nDCG;

	private BookmarkReader wikiReader;
	
	// used for averages
	public static double precisionSum = 0.0;
	public static double recallSum = 0.0;
	public static double fMeasureSum = 0.0;
	public static double mrrSum = 0.0;
	public static double mapSum = 0.0;	
	public static double userCoverageSum = 0.0;
	public static double diversitySum = 0.0;
	public static double serendipitySum = 0.0;
	public static double nDCGSum = 0.0;
	
	public MetricsCalculator(PredictionFileReader reader, String outputFile, int k, BookmarkReader wikiReader) {
		this.reader = reader;
		this.wikiReader = wikiReader;
		BufferedWriter bw = null;
		// Enable if you need data for statistical tests
		/*if (k == 5 || k == 10) {
			try {
				FileWriter writer = new FileWriter(new File(outputFile + "_" + k + ".txt"), true);
				bw = new BufferedWriter(writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		
		//double count = this.reader.getPredictionCount(); // only user where there are recommendations
		double count = this.reader.getPredictionData().size();		 // all users
		double recall = 0.0, precision = 0.0, mrr = 0.0, fMeasure = 0.0, map = 0.0;
		double diversity = 0.0, serendipity = 0.0;
		double nDCG = 0.0;
		
		List<Map<Integer, Double>> resourceTopics = null;
		int trainSize = 0;
		if (wikiReader != null) {
			trainSize = this.wikiReader.getCountLimit();
			// TODO: could be replaced by tags
//			resourceTopics = Utilities.getUniqueTopicMaps(wikiReader.getBookmarks().subList(0, trainSize), true);
			resourceTopics = Utilities.getRelativeTagMaps(wikiReader.getBookmarks().subList(0, trainSize), true);
		}		
		for (PredictionData data : this.reader.getPredictionData()) {
			if (data == null) {
				if (bw != null) {
					try {
						bw.write("0.0;0.0;0.0;0.0;0.0;0.0\n");
						//if (resourceTopics != null) {
						//	bw.write(Double.toString(data.getDiversity(resourceTopics)).replace('.', ',') + ";");		
						//}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				continue;
			}
			
			recall += data.getRecall();
			precision += data.getPrecision();
			fMeasure += data.getFMeasure();
			mrr += data.getMRR();
			map += data.getMAP();
			if (resourceTopics != null) {
				List<Integer> knownResources = Bookmark.getResourcesFromUser(wikiReader.getBookmarks().subList(0, trainSize), data.getUserID());
				diversity += data.getDiversity(resourceTopics);
				serendipity += data.getSerendipity(resourceTopics, knownResources);
			}
			nDCG += data.getNDCG();
			
			// TODO: check for diversity and serendipity
			if (bw != null) {
				try {
					bw.write(Double.toString(data.getRecall()).replace(',', '.') + ";");
					bw.write(Double.toString(data.getPrecision()).replace(',', '.') + ";");
					bw.write(Double.toString(data.getFMeasure()).replace(',', '.') + ";");
					bw.write(Double.toString(data.getMRR()).replace(',', '.') + ";");
					bw.write(Double.toString(data.getMAP()).replace(',', '.') + ";");
					bw.write(Double.toString(data.getNDCG()).replace(',', '.'));
					//if (resourceTopics != null) {
					//	bw.write(Double.toString(data.getDiversity(resourceTopics)).replace('.', ',') + ";");		
					//}
					bw.write("\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		this.recall = recall / count;
		this.precision = precision / count;
		this.fMeasure = fMeasure / count;
		this.mrr = mrr / count;
		this.map = map / count;		
		this.userCoverage = (double)this.reader.getPredictionCount() / (double)this.reader.getPredictionData().size();
		this.diversity = diversity / count;
		this.serendipity = serendipity / count;
		this.nDCG = nDCG / count;
		
		if (bw != null) {
			try {
				//bw.write("\n");
				bw.flush();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public double getRecall() {
		return this.recall;
	}
	
	public double getPrecision() {
		return this.precision;
	}
	
	public double getFMeasure() {
		return this.fMeasure;
	}
	
	public double getMRR() {
		return this.mrr;
	}
	
	public double getMAP() {
		return this.map;
	}
	
	public double getUserCoverage() {
		return this.userCoverage;
	}
	
	public double getDiversity() {
		return this.diversity;
	}
	
	public double getSerendipity() {
		return this.serendipity;
	}
	
	public double getNDCG(){
		return this.nDCG;
	}
	
	// Statics ----------------------------------------------------------------------------------------------------------------------
	
	public static void calculateMetrics(String filename, int k, String outputFile, boolean endline, BookmarkReader wikiReader, Integer minBookmarks, Integer maxBookmarks, Integer minResBookmarks, Integer maxResBookmarks, boolean calcTags) {
		PredictionFileReader reader = new PredictionFileReader();
		reader.readFile(filename, k, wikiReader, minBookmarks, maxBookmarks, minResBookmarks, maxResBookmarks);
		
		MetricsCalculator calc = new MetricsCalculator(reader, "./data/metrics/" + outputFile + "_all", k, calcTags ? null : wikiReader);
		recallSum += calc.getRecall();
		precisionSum += calc.getPrecision();
		fMeasureSum += calc.getFMeasure();
		mrrSum += calc.getMRR();
		mapSum += calc.getMAP();
		userCoverageSum += calc.getUserCoverage();
		diversitySum += calc.getDiversity();
		serendipitySum += calc.getSerendipity();
		nDCGSum += calc.getNDCG();
		
		/*
		if (outputFile != null) {
			try {
				FileWriter writer = new FileWriter(new File("./data/metrics/" + outputFile + "_all.txt"), true);
				BufferedWriter bw = new BufferedWriter(writer);
				bw.write(Double.toString(calc.getRecall()).replace('.', ',') + ";");
				bw.write(Double.toString(calc.getPrecision()).replace('.', ',') + ";");
				bw.write(Double.toString(calc.getFMeasure()).replace('.', ',') + ";");
				bw.write(Double.toString(calc.getMRR()).replace('.', ',') + ";");
				bw.write(Double.toString(calc.getMAP()).replace('.', ',') + (endline ? "\n" : ";"));			
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
	}
	
	public static void writeAverageMetrics(String outputFile, int k, double size, boolean calcTags, boolean endLine) {
		try {
			FileWriter writer = new FileWriter(new File("./data/metrics/" + outputFile + "_avg.txt"), true);
			BufferedWriter bw = new BufferedWriter(writer);
			double recall = recallSum / size;
			double precision = precisionSum / size;
			bw.write(Double.toString(recall).replace('.', ',') + ";");		
			bw.write(Double.toString(precision).replace('.', ',') + ";");		
			//bw.write(Double.toString((fMeasureSum / size)).replace('.', ',') + ";");
			bw.write(Double.toString(2.0 * recall * precision / (recall + precision == 0 ? 1.0 : recall + precision)).replace('.', ',') + ";");
			bw.write(Double.toString((mrrSum / size)).replace('.', ',') + ";");		
			bw.write(Double.toString((mapSum / size)).replace('.', ',') + ";");
			bw.write(Double.toString((nDCGSum / size)).replace('.', ',') + ";");
			bw.write(Double.toString((userCoverageSum / size)).replace('.', ','));
			if (!calcTags) {
				bw.write(";");
				bw.write(Double.toString((diversitySum / size)).replace('.', ',') + ";");		
				bw.write(Double.toString((serendipitySum / size)).replace('.', ',') + ";");
			}
			if (endLine)
				bw.write("\n");
			
			bw.write("\n");
			bw.close();
			
			resetMetrics();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void resetMetrics() {
		recallSum = 0.0;
		precisionSum = 0.0;
		fMeasureSum = 0.0;
		mrrSum = 0.0;
		mapSum = 0.0;
		userCoverageSum = 0.0;
		diversitySum = 0.0;
		serendipitySum = 0.0;
		nDCGSum = 0.0;
	}
	
	public static void calcF1Score(String dirName) {
		File dir = new File("./data/metrics/" + dirName);
		for (File file : dir.listFiles()) {
			FileReader reader;
			try {
				reader = new FileReader(file);
				BufferedReader br = new BufferedReader(reader);
				String line = null;
				int count = 0;
				while ((line = br.readLine()) != null) {
					if (++count == 5) {
						String[] lineParts = line.split(";");
						double recall = Double.parseDouble(lineParts[0].replace(",", "."));
						double precision = Double.parseDouble(lineParts[1].replace(",", "."));
						System.out.println(file.getName() + " " + (2.0 * recall * precision / (recall + precision)));
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
