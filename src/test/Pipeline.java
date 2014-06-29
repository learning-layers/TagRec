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

package test;

import java.util.ArrayList;
import java.util.List;

import common.CalculationType;

import processing.ActCalculator;
import processing.BM25Calculator;
import processing.BaselineCalculator;
import processing.FolkRankCalculator;
import processing.LanguageModelCalculator;
import processing.MalletCalculator;
import processing.MetricsCalculator;
import processing.RecCalculator;
import processing.ThreeLayersCalculator;
import file.BookmarkReader;
import file.BookmarkSplitter;

public class Pipeline {

	private static int TRAIN_SIZE;
	private static int TEST_SIZE;

	public static void main(String[] args) {
		System.out.println("TagRecommender:\n" + "" +
				"A framework to implement and evaluate algorithms for the recommendation\n" +
				"of tags." + 
			   	"Copyright (C) 2013 Dominik Kowald\n\n" + 
			   	"This program is free software: you can redistribute it and/or modify\n" + 
				" it under the terms of the GNU Affero General Public License as published by\n" +
				"the Free Software Foundation, either version 3 of the License, or\n" +
				"(at your option) any later version.\n\n" +							
				"This program is distributed in the hope that it will be useful,\n" +
				"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
				"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
				"GNU Affero General Public License for more details.\n\n" +							
				"You should have received a copy of the GNU Affero General Public License\n" +
				"along with this program.  If not, see <http://www.gnu.org/licenses/>.\n" + 
				"-----------------------------------------------------------------------------\n\n");
		
		// Testing
		startActCalculator("bib_core", "bib_core/bib_sample", 1, -5, -5, true, CalculationType.NONE);
		//startRecCalculator("bib_core", "bib_core/bib_sample");
		//startModelCalculator("bib_core", "bib_core/bib_sample", 1, -5);
		//startBaselineCalculator("bib_core", "bib_core/bib_sample", 1);
		//startCfTagCalculator("bib_core", "bib_core/bib_sample", 1, 20, -5);
		//startFolkRankCalculator("bib_core", "bib_core/bib_sample", 1);
		//startLdaCalculator("bib_core", "bib_core/bib_sample", 100, 1);
		//start3LayersJavaCalculator("bib_core", "bib_core/bib_sample", "", 1, -5);
				
		if (args.length < 3) {
			System.out.println("Too few arguments!");
			return;
		}
		String op = args[0];
		String samplePath = "", sampleDir = "";
		int sampleCount = 1;
		if (args[1].equals("cul")) {
			samplePath = "cul_core/" + args[2];
			sampleDir = "cul_core";
		} else if (args[1].equals("flickr")) {
			samplePath = "flickr_core/" + args[2];
			sampleDir = "flickr_core";
		} else if (args[1].equals("bib")) {
			samplePath = "bib_core/" + args[2];
			sampleDir = "bib_core";
		} else if (args[1].equals("wiki")) {
			samplePath = "wiki_core/" + args[2];
			sampleDir = "wiki_core";
		} else {
			System.out.println("Dataset not available");
			return;
		}

		/*if (op.equals("split")) {
			System.out.println("Start splitting");
			if (args.length == 3) {
				WikipediaSplitter.splitSample(dataset, samplePath, sampleCount,
						param1);
			} else if (args.length == 5) {
				WikipediaSplitter.splitSample(dataset, samplePath, sampleCount,
						param1, Integer.parseInt(args[3]),
						Integer.parseInt(args[4]));
			}
		} else */if (op.equals("cf")) {
			startCfTagCalculator(sampleDir, samplePath, sampleCount, 20, -5);
		} else if (op.equals("fr")) {
			startFolkRankCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("bll_c")) {
			startActCalculator(sampleDir, samplePath, sampleCount, -5, -5, true, CalculationType.NONE);
		} else if (op.equals("bll_c_ac")) {
			startActCalculator(sampleDir, samplePath, sampleCount, -5, -5, true, CalculationType.USER_TO_RESOURCE);
		} else if (op.equals("girptm")) {
			startRecCalculator(sampleDir, samplePath);
		} else if (op.equals("mp_ur")) {
			startModelCalculator(sampleDir, samplePath, sampleCount, -5);
		} else if (op.equals("mp")) {
			startBaselineCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("3layers")) {
			start3LayersJavaCalculator(sampleDir, samplePath, "lda_500", sampleCount, -5, -5, true, false, false);
		} else if (op.equals("3LT")) {
			start3LayersJavaCalculator(sampleDir, samplePath, "lda_500", sampleCount, -5, -5, true, true, false);
			start3LayersJavaCalculator(sampleDir, samplePath, "lda_500", sampleCount, -5, -5, true, false, true);
		} else if (op.equals("lda")) {
			startLdaCalculator(sampleDir, samplePath, 1000, sampleCount);
		} else if (op.equals("lda_samples")) {
			createLdaSamples(samplePath, sampleCount, 500);
		} else if (op.equals("core")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 3);
		} else if (op.equals("split")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 3);
		}
	}

	private static void startActCalculator(String sampleDir, String sampleName,
			int sampleCount, int dUpperBound, int betaUpperBound, boolean all, CalculationType type) {
		getTrainTestSize(sampleName);
		List<Integer> dValues = getBetaValues(dUpperBound);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		String ac = type == CalculationType.USER_TO_RESOURCE ? "_ac" : "";
		
		for (int i = 1; i <= sampleCount; i++) {
			for (int dVal : dValues) {
				ActCalculator.predictSample(sampleName, TRAIN_SIZE,
						TEST_SIZE, true, false, dVal, 5, type);
				writeMetrics(sampleDir, sampleName,
						"bll" + ac + "_" + 5 + "_" + dVal, sampleCount, 10, null);
				if (all) {
					for (int betaVal : betaValues) {
						ActCalculator.predictSample(sampleName,
								TRAIN_SIZE, TEST_SIZE, true, true, dVal,
								betaVal, type);
						writeMetrics(sampleDir, sampleName, "bll_c" + ac + "_" + betaVal
								+ "_" + dVal, sampleCount, 10, null);
					}
					ActCalculator.predictSample(sampleName,
							TRAIN_SIZE, TEST_SIZE, false, true, dVal, 5, type);
					writeMetrics(sampleDir, sampleName, "bll_r" + ac + "_" + 5 + "_"
							+ dVal, sampleCount, 10, null);
				}
			}
		}
		// n, p, q
	}

	private static void startRecCalculator(String sampleDir, String sampleName) {
		getTrainTestSize(sampleName);
		RecCalculator.predictSample(sampleName, TRAIN_SIZE,
				TEST_SIZE, true, false);
		writeMetrics(sampleDir, sampleName, "girp", 1, 10, null);
		RecCalculator.predictSample(sampleName, TRAIN_SIZE,
				TEST_SIZE, true, true);
		writeMetrics(sampleDir, sampleName, "girptm", 1, 10, null);
		// l, m
	}

	private static void startModelCalculator(String sampleDir,
			String sampleName, int sampleCount, int betaUpperBound) {
		getTrainTestSize(sampleName);
		List<Integer> betaValues = getBetaValues(betaUpperBound);

		for (int i = 1; i <= sampleCount; i++) {
			LanguageModelCalculator.predictSample(sampleName,
					TRAIN_SIZE, TEST_SIZE, true, false, 5);
			LanguageModelCalculator.predictSample(sampleName,
					TRAIN_SIZE, TEST_SIZE, false, true, 5);
		}
		writeMetrics(sampleDir, sampleName, "mp_u_" + 5, sampleCount, 10,
				null);
		writeMetrics(sampleDir, sampleName, "mp_r_" + 5, sampleCount, 10,
				null);
		for (int beta : betaValues) {
			for (int i = 1; i <= sampleCount; i++) {
				LanguageModelCalculator.predictSample(sampleName,
						TRAIN_SIZE, TEST_SIZE, true, true, beta);
			}
			writeMetrics(sampleDir, sampleName, "mp_ur_" + beta, sampleCount,
					10, null);
		}
		// b, c, d
	}

	private static void startCfTagCalculator(String sampleDir,
			String sampleName, int sampleCount, int neighbors,
			int betaUpperBound) {
		getTrainTestSize(sampleName);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		for (int i = 1; i <= sampleCount; i++) {
			BM25Calculator.predictTags(sampleName, TRAIN_SIZE,
					TEST_SIZE, neighbors, true, false, 5);
			BM25Calculator.predictTags(sampleName, TRAIN_SIZE,
					TEST_SIZE, neighbors, false, true, 5);
		}
		writeMetrics(sampleDir, sampleName, "usercf_" + 5, sampleCount, 10,
				null);
		writeMetrics(sampleDir, sampleName, "rescf_" + 5, sampleCount, 10, null);
		for (int beta : betaValues) {
			for (int i = 1; i <= sampleCount; i++) {
				BM25Calculator.predictTags(sampleName, TRAIN_SIZE,
						TEST_SIZE, neighbors, true, true, beta);
			}
			writeMetrics(sampleDir, sampleName, "cf_" + beta, sampleCount, 10,
					null);
		}
		// e, f, g
	}

	private static void startFolkRankCalculator(String sampleDir,
			String sampleName, int size) {
		getTrainTestSize(sampleName);
		for (int i = 1; i <= size; i++) {
			FolkRankCalculator.predictSample(sampleName, TRAIN_SIZE,
					TEST_SIZE, true);
		}
		writeMetrics(sampleDir, sampleName, "fr", size, 10, null);
		writeMetrics(sampleDir, sampleName, "apr", size, 10, null);
		// "k_fr", "j_pr"
	}

	private static void startBaselineCalculator(String sampleDir,
			String sampleName, int size) {
		getTrainTestSize(sampleName);
		for (int i = 1; i <= size; i++) {
			BaselineCalculator.predictPopularTags(sampleName,
					TRAIN_SIZE, TEST_SIZE);
		}
		writeMetrics(sampleDir, sampleName, "mp", size, 10, null);
	}

	private static void startLdaCalculator(String sampleDir, String sampleName, int topics, int sampleCount) {
		getTrainTestSize(sampleName);
		for (int i = 1; i <= sampleCount; i++) {
			MalletCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, topics, true, true);
		}
		writeMetrics(sampleDir, sampleName, "lda_" + topics, sampleCount, 10, null);
		// h
	}
	
	private static void createLdaSamples(String sampleName, int size, int topics) {
		getTrainTestSize(sampleName + "_" + 1);
		for (int i = 1; i <= size; i++) {
			MalletCalculator.createSample(sampleName + "_" + i, TEST_SIZE, (short)topics, false, true);			
		}
	}
		
	private static void start3LayersJavaCalculator(String sampleDir, String sampleName, String topicString, int size, int dUpperBound, int betaUpperBound, boolean resBased, boolean tagBLL, boolean topicBLL) {
		getTrainTestSize(sampleName);
		List<Integer> dValues = getBetaValues(dUpperBound);
		List<Integer> betaValues = getBetaValues(betaUpperBound);		
		String suffix = "layers";
		if (tagBLL && topicBLL) {
			suffix += "bll";
		} else if (tagBLL) {
			suffix += "tagbll";
		} else if (topicBLL) {
			suffix += "topicbll";
		}
		
		for (int i = 1; i <= size; i++) {
			for (int d : dValues) {
				if (resBased) {
					for (int b : betaValues) {
						ThreeLayersCalculator.predictSample(sampleName + "_" + i + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, d, b, true, true, tagBLL, topicBLL);
						writeMetrics(sampleDir, sampleName, suffix + "_" + b + "_" + d, size, 10, !topicString.isEmpty() ? topicString : null);
					}
				}
				ThreeLayersCalculator.predictSample(sampleName + "_" + i + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, d, 5, true, false, tagBLL, topicBLL);
				writeMetrics(sampleDir, sampleName, "user" + suffix + "_" + 5 + "_" + d, size, 10, !topicString.isEmpty() ? topicString : null);
			}
		}
	}
	
	// Helpers
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------
	private static void writeMetrics(String sampleDir, String sampleName,
			String prefix, int sampleCount, int k, String posfix) {
		String topicString = ((posfix == null || posfix == "0") ? "" : "_"
				+ posfix);
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= sampleCount; j++) {
				MetricsCalculator.calculateMetrics(sampleName
						+ topicString + "_" + prefix, i, sampleDir + "/"
						+ prefix + topicString + "_metrics", false);
			}
			MetricsCalculator.writeAverageMetrics(sampleDir + "/" + prefix
					+ topicString + "_metrics", i, (double) sampleCount);
		}
		MetricsCalculator.resetMetrics();
	}

	private static List<Integer> getBetaValues(int betaUpperBound) {
		List<Integer> betaValues = new ArrayList<Integer>();
		if (betaUpperBound < 0) {
			betaValues.add(betaUpperBound * (-1));
		} else {
			for (int betaVal = 1; betaVal <= betaUpperBound; betaVal++) {
				betaValues.add(betaVal);
			}
		}
		return betaValues;
	}

	private static void getStatistics(String dataset) {
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(dataset);
		int bookmarks = reader.getBookmarks().size();
		System.out.println("Bookmarks: " + bookmarks);
		int users = reader.getUsers().size();
		System.out.println("Users: " + users);
		int resources = reader.getResources().size();
		System.out.println("Resources: " + resources);
		int tags = reader.getTags().size();
		System.out.println("Tags: " + tags);
		int tagAssignments = reader.getTagAssignmentsCount();
		System.out.println("Tag-Assignments: " + tagAssignments);
	}

	private static void getTrainTestSize(String sample) {
		BookmarkReader trainReader = new BookmarkReader(-1, false);
		trainReader.readFile(sample + "_train");
		TRAIN_SIZE = trainReader.getBookmarks().size();
		System.out.println("Train-size: " + TRAIN_SIZE);
		BookmarkReader testReader = new BookmarkReader(-1, false);
		testReader.readFile(sample + "_test");
		TEST_SIZE = testReader.getBookmarks().size();
		System.out.println("Test-size: " + TEST_SIZE);
	}
}
