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
import file.postprocessing.CatDescFiltering;

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

	private BookmarkReader bookmarkReader;
	
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
	
	public MetricsCalculator(PredictionFileReader reader, String outputFile, int k, BookmarkReader bookmarkReader, boolean recommTags) {
		this.reader = reader;
		if (recommTags) { // TODO: check
			this.bookmarkReader = bookmarkReader;
		}
		BufferedWriter bw = null;
		//TODO: Enable if you need data for statistical tests
		if ((recommTags && (k == 5 || k == 10)) || (!recommTags && k == 20)) {
			try {
				FileWriter writer = new FileWriter(new File(outputFile + "_" + k + ".txt"), true);
				bw = new BufferedWriter(writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//double count = this.reader.getPredictionCount(); // only user where there are recommendations
		double count = this.reader.getPredictionData().size();		 // all users
		double recall = 0.0, precision = 0.0, mrr = 0.0, fMeasure = 0.0, map = 0.0, nDCG = 0.0, diversity = 0.0, serendipity = 0.0;
		
		List<Map<Integer, Double>> entityFeatures = null;
		List<Map<Integer, Integer>> tagCountMaps = null;
		List<Bookmark> trainList = null;
		if (this.bookmarkReader != null) {
			trainList = this.bookmarkReader.getBookmarks().subList(0, this.bookmarkReader.getCountLimit());
			if (recommTags) {
				tagCountMaps = Utilities.getResMaps(trainList);
				entityFeatures = Utilities.getResourceMapsForTags(trainList);
			} else {
				entityFeatures = Utilities.getUniqueTopicMaps(trainList, true); // TODO: check regarding unique!
			}
		}
		// process each predicted line
		for (PredictionData data : this.reader.getPredictionData()) {
			if (data == null) {
				if (bw != null) {
					try {
						bw.write("0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				continue;
			}
			double cRecall = data.getRecall();
			recall += cRecall;
			double cPrecision = data.getPrecision(recommTags);
			precision += cPrecision;
			double cFMeasure = data.getFMeasure(recommTags);
			fMeasure += cFMeasure;
			double cMRR = data.getMRR();
			mrr += cMRR;
			double cMAP = data.getMAP();
			map += cMAP;
			double cNDCG = data.getNDCG();
			nDCG += cNDCG;
			double cDiversity = 0.0, cSerendipity = 0.0;
			if (this.bookmarkReader != null) {
				if (recommTags) {
					cDiversity = data.getTagDiversity(entityFeatures);
					if (data.getResID() < tagCountMaps.size()) {
						Map<Integer, Integer> tagCountMap = tagCountMaps.get(data.getResID());
						cSerendipity = data.getTagSerendipity(tagCountMap, false);
					} else {
						cSerendipity = 1.0;
					}
				} else {
					List<Integer> knownEntities = Bookmark.getResourcesFromUser(trainList, data.getUserID());
					cDiversity = data.getDiversity(entityFeatures, true);
					cSerendipity = data.getSerendipity(entityFeatures, knownEntities);
				}
				diversity += cDiversity;
				serendipity += cSerendipity;
			}
			
			if (bw != null) {
				try {
					bw.write(Double.toString(cRecall).replace(',', '.') + ";");
					bw.write(Double.toString(cPrecision).replace(',', '.') + ";");
					bw.write(Double.toString(cFMeasure).replace(',', '.') + ";");
					bw.write(Double.toString(cMRR).replace(',', '.') + ";");
					bw.write(Double.toString(cMAP).replace(',', '.') + ";");
					bw.write(Double.toString(cNDCG).replace(',', '.') + ";");
					bw.write(Double.toString(data.getCoverage()).replace(',', '.') + ";");
					bw.write(Double.toString(cDiversity).replace('.', ',') + ";");
					bw.write(Double.toString(cSerendipity).replace('.', ','));	
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
		this.nDCG = nDCG / count;
		this.userCoverage = (double)this.reader.getPredictionCount() / (double)this.reader.getPredictionData().size();
		this.diversity = diversity / count;
		this.serendipity = serendipity / count;
		
		// TODO: enable in case statistics are needed
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
	
	public double getNDCG(){
		return this.nDCG;
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
	
	// Statics ----------------------------------------------------------------------------------------------------------------------
	public static void calculateMetrics(String filename, int k, String outputFile, boolean endline, BookmarkReader bookmarkReader, Integer minBookmarks, Integer maxBookmarks, Integer minResBookmarks, Integer maxResBookmarks, CatDescFiltering describer, boolean calcTags, Integer trainSize) {
		PredictionFileReader reader = new PredictionFileReader();
		if (trainSize == null) {
			reader.readFile(filename, k, bookmarkReader, minBookmarks, maxBookmarks, minResBookmarks, maxResBookmarks, describer);
		} else { // means my MyMediaLite files
			if (calcTags) {
				reader.readTensorFile(filename, k, trainSize, bookmarkReader, minBookmarks, maxBookmarks, minResBookmarks, maxResBookmarks, describer);
			} else {
				reader.readMyMediaLiteFile(filename, k, trainSize.intValue(), bookmarkReader, minBookmarks, maxBookmarks, minResBookmarks, maxResBookmarks, describer);
			}
		}
		String suffix = "";
		if (describer != null) {
			suffix += ("_" + (describer.getDescriber() ? "desc" : "cat"));
		}
		
		MetricsCalculator calc = new MetricsCalculator(reader, "./data/metrics/" + outputFile + suffix + "_all", k, bookmarkReader, calcTags);
		recallSum += calc.getRecall();
		precisionSum += calc.getPrecision();
		fMeasureSum += calc.getFMeasure();
		mrrSum += calc.getMRR();
		mapSum += calc.getMAP();
		userCoverageSum += calc.getUserCoverage();
		diversitySum += calc.getDiversity();
		serendipitySum += calc.getSerendipity();
		nDCGSum += calc.getNDCG();
	}
	
	public static void writeAverageMetrics(String outputFile, int k, double size, boolean calcTags, boolean endLine, Boolean describer) {
		try {
			// TODO: add also suffix for minBookmarks post-processing
			String suffix = "";
			if (describer != null) {
				suffix += ("_" + (describer.booleanValue() ? "desc" : "cat"));
			}
			FileWriter writer = new FileWriter(new File("./data/metrics/" + outputFile + suffix + "_avg.txt"), true);
			BufferedWriter bw = new BufferedWriter(writer);
			double recall = recallSum / size;
			double precision = precisionSum / size;
			bw.write(Double.toString(recall).replace('.', ',') + ";");		
			bw.write(Double.toString(precision).replace('.', ',') + ";");		
			//bw.write(Double.toString((fMeasureSum / size)).replace('.', ',') + ";");
			bw.write(Double.toString(2.0 * recall * precision / ((recall + precision) == 0 ? 1.0 : (recall + precision))).replace('.', ',') + ";");
			bw.write(Double.toString((mrrSum / size)).replace('.', ',') + ";");		
			bw.write(Double.toString((mapSum / size)).replace('.', ',') + ";");
			bw.write(Double.toString((nDCGSum / size)).replace('.', ',') + ";");
			bw.write(Double.toString((userCoverageSum / size)).replace('.', ','));
			//if (!calcTags) {
				bw.write(";");
				bw.write(Double.toString((diversitySum / size)).replace('.', ',') + ";");		
				bw.write(Double.toString((serendipitySum / size)).replace('.', ','));
			//}
			
			if (endLine) {
				bw.write("\n");
			}		
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
		nDCGSum = 0.0;
		userCoverageSum = 0.0;
		diversitySum = 0.0;
		serendipitySum = 0.0;
	}
	
	// could be used to manually calculate F1-score
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
