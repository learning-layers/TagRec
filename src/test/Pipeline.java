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

import itemrecommendations.CFResourceCalculator;
import itemrecommendations.HuangCalculator;
import itemrecommendations.CIRTTCalculator;
import itemrecommendations.MPResourceCalculator;
import itemrecommendations.SustainCalculator;
import itemrecommendations.ZhengCalculator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import common.Bookmark;
import common.CalculationType;
import common.Features;
import common.SolrConnector;
import common.Utilities;
import processing.BLLCalculator;
import processing.CFTagRecommender;
import processing.MPCalculator;
import processing.ContentBasedCalculator;
import processing.FolkRankCalculator;
import processing.MPurCalculator;
import processing.MalletCalculator;
import processing.MetricsCalculator;
import processing.GIRPTMCalculator;
import processing.RecencyCalculator;
import processing.SolrHashtagCalculator;
import processing.ThreeLTCalculator;
import processing.analyzing.TagReuseProbAnalyzer;
import processing.analyzing.UserTagDistribution;
import engine.Algorithm;
import engine.BaseLevelLearningEngine;
import engine.EntityRecommenderEngine;
import engine.EntityType;
import engine.ResourceRecommenderEngine;
import engine.UserRecommenderEngine;
import engine.EngineInterface;
import engine.LanguageModelEngine;
import engine.TagRecommenderEvalEngine;
import engine.ThreeLayersEngine;
import file.BookmarkReader;
import file.BookmarkSplitter;
import file.postprocessing.CatDescFiltering;
import file.preprocessing.BibsonomyProcessor;
import file.preprocessing.CiteULikeProcessor;
import file.preprocessing.JSONProcessor;
import file.preprocessing.PintsProcessor;
import file.preprocessing.LastFMProcessor;
import file.preprocessing.MovielensProcessor;
import file.preprocessing.TensorProcessor;

public class Pipeline {
	
	// are set automatically in code
	private static int TRAIN_SIZE;
	private static int TEST_SIZE;
	// set for postprocessing (number of bookmarks - null is nothing)
	private final static Integer MIN_USER_BOOKMARKS = null;
	private final static Integer MAX_USER_BOOKMARKS = null;
	private final static Integer MIN_RESOURCE_BOOKMARKS = null;
	private final static Integer MAX_RESOURCE_BOOKMARKS = null;
	// set for categorizer/describer split (true is describer, false is categorizer - null for nothing)
	private final static Boolean DESCRIBER = null;
	// placeholder for the topic posfix
	private static String TOPIC_NAME = null;
	// placeholder for the used dataset
	private final static String DATASET = "twitter";
	private final static String SUBDIR = "/researchers";
	
	public static void main(String[] args) {
		System.out.println("TagRecommender:\n" + "" +
				"A framework to implement and evaluate algorithms for the recommendation\n" +
				"of tags." + 
			   	"Copyright (C) 2013 - 2015 Dominik Kowald\n\n" + 
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
		String dir = DATASET + "_core" + SUBDIR;
		String path = dir + "/" + DATASET + "_sample";
		
		//TOPIC_NAME = "lda_500";
		//startCfResourceCalculator(dir, path, 1, 20, false, true, false, false, Features.TOPICS);
		
		//startSolrHashtagCalculator(dir, "http://kti-social:8938", "researcher", false, true, 24);
		
		//BibsonomyProcessor.processUnsortedFile("dc09_core/test_core/", "tas", "dc09_sample_test");
		//MovielensProcessor.processFile("000_dataset_dump/tags.dat", "000_dataset_dump/movielens", "000_dataset_dump/ratings.dat");
		//getTrainTestSize(path);
		//TensorProcessor.writeFiles(path, TRAIN_SIZE, TEST_SIZE, true, null, null, null);
		
		//getTrainTestSize(path);
		//TagReuseProbAnalyzer.analyzeSample(path, TRAIN_SIZE, TEST_SIZE, false);
		
		//evaluate(dir, path, "pitf", TOPIC_NAME, true, true, null);
		
		//JSONProcessor.writeJSONOutput("bib_core/vedran/bib_bibtex_2_perc_1");
		
		//evaluateAllTagRecommenderApproaches(dir, path);
		//startAllTagRecommenderApproaches(dir, path, true);
		//getTrainTestStatistics(path);
		//BookmarkSplitter.splitSample(dir + "/tweets", dir + "/twitter_sample", 1, 0, true, false, true, dir + "/white_user.txt");
		//try { getStatistics("sss-eval", false); } catch (Exception e) { e.printStackTrace(); }
		//BookmarkSplitter.drawUserPercentageSample("bib_core/vedran/bib_bibtex", 5);
		//createLdaSamples("ml_core/resource/ml_sample", 1, 500, true, true);
		
		// Method Testing -> just uncomment the methods you want to test
		// Test the BLL and BLL+MP_r algorithms (= baseline to beat :))
		//startActCalculator(dir, path, 1, 15, -5, false, CalculationType.NONE, false);
		
		// Test the BLL_AC and BLL_AC+MP_r algorithms (could take a while)

		//startActCalculator(dir, path, 1, -5, -5, false, CalculationType.USER_TO_RESOURCE_ONLY, false);
		//startActCalculator(dir, path, 1, -5, -5, false, CalculationType.USER_TO_RESOURCE, false);
		
		//startRecCalculator(dir, path);
		
		// Test the GIRP and GIRPTM algorithms
		//startGirpCalculator(dir, path, false);
		
		// Test the MP_u, MP_r and MP_u_r algorithms
		//startModelCalculator(dir, path, 1, -5, false);
		
		// Test the MP algorithm
		//startBaselineCalculator(dir, path, 1, true);
		
		// Test the CF_u, CF_r and CF_u_r algorithms with 20 neighbors (change it if you want)
		//startCfTagCalculator(dir, path, 1, 20, -5, false);
		
		// Test the PR and FR algorithms
		//startFolkRankCalculator(dir, path, 1);
		
		// Test the LDA algorithm with 1000 topics (change it if you want)
		//startLdaCalculator(dir, path, 1000, 1, false);
		
		// Test the 3L algorithm
		//start3LayersJavaCalculator(dir, path, "", 1, -5, -5, true, false, false);
		
		// Test the 3L_tag algorithm
		//start3LayersJavaCalculator(dir, path, "", 1, -5, -5, true, true, false);
		
		// Test the 3LT_topic algorithm
		//start3LayersJavaCalculator(dir, path, "lda_1000", 1, -5, -5, true, false, true);
		
		// TODO: just execute to test your recommender - results can be found in metrics/bib_core
		//startContentBasedCalculator(dir, path);
		
		// Resource-Recommender testing
		//writeTensorFiles(path, true);
		//evaluate(dir, path, "wrmf_500_mml", TOPIC_NAME, false, true, null);
		//createLdaSamples(path, 1, 500, false);
		//startCfResourceCalculator(dir, path, 1, 20, true, false, false, false, Features.ENTITIES);
		//startCfResourceCalculator(dir, path, 1, 20, false, true, true, false, Features.ENTITIES);
		//startCfResourceCalculator(dir, path, 1, 20, false, true, false, false, Features.TOPICS);
		//startResourceCIRTTCalculator(dir, path, "", 1, 20, Features.ENTITIES, false, true, false, true);
		//startBaselineCalculatorForResources(dir, path, 1, false);
		//startSustainApproach(dir, path, 2.845, 0.5, 6.396, 0.0936, 0, 0, 20, 0.5);
			
		// Engine Testing
		//startEngineTest(null, "sss_recomm");
		//startKnowBrainTest("ml_group2");
		
		// Commandline Arguments
		if (args.length < 3) {
			System.out.println("Too few arguments!");
			return;
		}
		String op = args[0];
		String samplePath = "", sampleDir = "";
		int sampleCount = 1;
		String subdir = "/";
		if (args[1].equals("cul")) {
			sampleDir = "cul_core";
		} else if (args[1].equals("flickr")) {
			sampleDir = "flickr_core";
		} else if (args[1].equals("bib")) {
			sampleDir = "bib_core";
		} else if (args[1].equals("wiki")) {
			sampleDir = "wiki_core";
		} else if (args[1].equals("ml")) {
			sampleDir = "ml_core";
		} else if (args[1].equals("lastfm")) {
			sampleDir = "lastfm_core";
		} else if (args[1].equals("del")) {
			sampleDir = "del_core";
		} else if (args[1].equals("twitter_res")) {
			sampleDir = "twitter_core";
			subdir = "/researchers";
		} else if (args[1].equals("twitter_gen")) {
			sampleDir = "twitter_core";
			subdir = "/general";
		} else {
			System.out.println("Dataset not available");
			return;
		}
		sampleDir += subdir;
		samplePath += (sampleDir + "/" + args[2]);
		
		boolean narrowFolksonomy = args[1].equals("flickr") || args[1].contains("twitter");
		if (op.equals("cf")) {
			startCfTagCalculator(sampleDir, samplePath, sampleCount, 20, -5, false);
		} else if (op.equals("cfr")) {
			startCfTagCalculator(sampleDir, samplePath, sampleCount, 20, -5, !narrowFolksonomy);
		} else if (op.equals("fr")) {
			startFolkRankCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("bll_c")) {
			startActCalculator(sampleDir, samplePath, sampleCount, -5, -5, !narrowFolksonomy, CalculationType.NONE, true);
		} else if (op.equals("bll_c_ac")) {
			if (!narrowFolksonomy) {
				startActCalculator(sampleDir, samplePath, sampleCount, -5, -5, !narrowFolksonomy, CalculationType.USER_TO_RESOURCE, true);
			}
		} else if (op.equals("girptm")) {
			startGirpCalculator(sampleDir, samplePath, !narrowFolksonomy);
		} else if (op.equals("mp_ur")) {
			startModelCalculator(sampleDir, samplePath, sampleCount, -5, !narrowFolksonomy);
		} else if (op.equals("mp")) {
			startBaselineCalculator(sampleDir, samplePath, sampleCount, true);
		} else if (op.equals("3layers")) {
			start3LayersJavaCalculator(sampleDir, samplePath, "", sampleCount, -5, -5, !narrowFolksonomy, false, false);
		} else if (op.equals("3LT")) {
			start3LayersJavaCalculator(sampleDir, samplePath, "", sampleCount, -5, -5, !narrowFolksonomy, true, false);
			start3LayersJavaCalculator(sampleDir, samplePath, "", sampleCount, -5, -5, !narrowFolksonomy, false, true);
		} else if (op.equals("lda")) {
			startLdaCalculator(sampleDir, samplePath, 1000, sampleCount, !narrowFolksonomy);
		} else if (op.equals("lda_samples")) {
			createLdaSamples(samplePath, sampleCount, 1000, true, false);
		} else if (op.equals("tensor_samples")) {
			writeTensorFiles(samplePath, true);
		} else if (op.equals("mymedialite_samples")) {
			writeTensorFiles(samplePath, false);
		} else if (op.equals("core")) {
			BookmarkSplitter.calculateCore(samplePath, samplePath, 3, 3, 3);
		} else if (op.equals("split_l1o")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 0, true, false, true, null);
		} else if (op.equals("split_8020")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 20, false, false, true, null);
		} else if (op.equals("percentage_sample")) {
			BookmarkSplitter.drawUserPercentageSample(samplePath, 3, 1);
		} else if (op.equals("core_sample_splits")) {
			sampleCount = 4;
			for (int i = 1; i <= sampleCount; i++) {
				if (!narrowFolksonomy) {
					BookmarkSplitter.calculateCore(samplePath + i, samplePath + "_c3" + i, 3, 3, 3);
				} else {
					BookmarkSplitter.calculateCore(samplePath + i, samplePath + "_c3" + i, 3, 1, 3);
				}
				BookmarkSplitter.splitSample(samplePath + "_c3" + i, samplePath + "_c3" + i, 1, 0, true, true, true, null);
			}
		} else if (op.equals("percentage_sample_splits")) {
			sampleCount = 4;
			BookmarkSplitter.drawUserPercentageSample(samplePath, 3, sampleCount);
			for (int i = 1; i <= sampleCount; i++) {
				BookmarkSplitter.splitSample(samplePath + i, samplePath + i, 1, 0, true, true, true, null);
			}
		} else if (op.equals("process_bibsonomy")) {
			BibsonomyProcessor.processUnsortedFile(sampleDir, "tas", args[2]);
		} else if (op.equals("process_citeulike")) {	
			CiteULikeProcessor.processFile("current", args[2]);
		} else if (op.equals("process_lastfm")) {	
			LastFMProcessor.processFile("user_taggedartists-timestamps.dat", args[2]);
		} else if (op.equals("process_ml")) {
			MovielensProcessor.processFile("tags.dat", args[2], "ratings.dat");	
		} else if (op.equals("process_del")) {
			PintsProcessor.processFile(sampleDir, "delicious", args[2]);	
		} else if (op.equals("process_flickr")) {
			PintsProcessor.processFile(sampleDir, "flickr", args[2]);	
		} else if (op.equals("item_mp")) {
			startBaselineCalculatorForResources(sampleDir, samplePath, sampleCount, false);
		} else if (op.equals("item_cft")) {
			startCfResourceCalculator(sampleDir, samplePath, sampleCount, 20, true, false, false, false, Features.TAGS);
		} else if (op.equals("item_cfb")) {
			startCfResourceCalculator(sampleDir, samplePath, sampleCount, 20, true, false, false, false, Features.ENTITIES);
		} else if (op.equals("item_cbt")) {
			TOPIC_NAME = "lda_500";
			startCfResourceCalculator(sampleDir, samplePath, 1, 20, false, true, false, false, Features.TOPICS);
		} else if (op.equals("item_zheng")) {
			startZhengResourceCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("item_huang")) {
			startHuangResourceCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("item_cirtt")) {
			startResourceCIRTTCalculator(sampleDir, samplePath, "", sampleCount, 20, Features.ENTITIES, false, true, false, true);
		} else if (op.equals("item_sustain")) {
			startSustainApproach(dir, path, 2.845, 0.5, 6.396, 0.0936, 0, 0, 20, 0.5);
		} else if (op.equals("tag_all")) {
			startAllTagRecommenderApproaches(sampleDir, samplePath, !narrowFolksonomy);
		} else if (op.equals("tag_samples")) {
			startSampleTagRecommenderApproaches(sampleDir, samplePath + "1", !narrowFolksonomy);
			startSampleTagRecommenderApproaches(sampleDir, samplePath + "2", !narrowFolksonomy);
			startSampleTagRecommenderApproaches(sampleDir, samplePath + "3", !narrowFolksonomy);
			startSampleTagRecommenderApproaches(sampleDir, samplePath + "4", !narrowFolksonomy);
		} else if (op.equals("twitter")) {
			startBaselineCalculator(sampleDir, samplePath, sampleCount, true);
			startModelCalculator(sampleDir, samplePath, sampleCount, -5, !narrowFolksonomy);
			startGirpCalculator(sampleDir, samplePath, !narrowFolksonomy);
			startActCalculator(sampleDir, samplePath, sampleCount, -5, -5, !narrowFolksonomy, CalculationType.NONE, true);
			startCfTagCalculator(sampleDir, samplePath, sampleCount, 20, -5, false);
			startFolkRankCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("stats")) {
			try { getStatistics(samplePath, false); } catch (Exception e) { e.printStackTrace(); }
		} else {
			System.out.println("Unknown operation");
		}
	}

	// Tag Recommenders methods ---------------------------------------------------------------------------------------------------------------------------------------------	
	private static void startSolrHashtagCalculator(String sampleDir, String solrUrl, String sampleName, boolean train, boolean hours, Integer mostRecentTweets) {
		if (train) {
			String suffix = SolrHashtagCalculator.predictTrainSample(sampleDir, sampleName, solrUrl, hours, mostRecentTweets);
			writeMetrics(sampleDir, sampleDir + "/" + sampleName, suffix, 1, 10, null, null, null);
		} else {
			String suffix = SolrHashtagCalculator.predictSample(sampleDir, sampleName, solrUrl);
			writeMetrics(sampleDir, sampleDir + "/" + sampleName, suffix, 1, 10, null, null, null);
		}
	}
	
	private static void startAllTagRecommenderApproaches(String sampleDir, String samplePath, boolean all) {
		startBaselineCalculator(sampleDir, samplePath, 1, true);		// MP
		startModelCalculator(sampleDir, samplePath, 1, -5, all);		// MPur
		startGirpCalculator(sampleDir, samplePath, all);					// GIRPTM
		startActCalculator(sampleDir, samplePath, 1, -5, -5, all, CalculationType.NONE, true);				// BLL
		startActCalculator(sampleDir, samplePath, 1, -5, -5, all, CalculationType.USER_TO_RESOURCE, true);	// BLLac
		start3LayersJavaCalculator(sampleDir, samplePath, "", 1, -5, -5, all, false, false);				// 3L		
		start3LayersJavaCalculator(sampleDir, samplePath, "", 1, -5, -5, all, true, false);					// 3LTtop
		start3LayersJavaCalculator(sampleDir, samplePath, "", 1, -5, -5, all, false, true);					// 3LTtag
		startCfTagCalculator(sampleDir, samplePath, 1, 20, -5, false);	// CFur
		startFolkRankCalculator(sampleDir, samplePath, 1);				// APR+FR
		startLdaCalculator(sampleDir, samplePath, 1000, 1, all);		// LDA
	}
	
	private static void startSampleTagRecommenderApproaches(String sampleDir, String samplePath, boolean all) {
		startModelCalculator(sampleDir, samplePath, 1, -5, all);											// MPur
		startGirpCalculator(sampleDir, samplePath, all);														// GIRPTM
		startActCalculator(sampleDir, samplePath, 1, -5, -5, all, CalculationType.USER_TO_RESOURCE, false);	// BLLac
		startFolkRankCalculator(sampleDir, samplePath, 1);													// APR+FR
	}
	
	private static void evaluateAllTagRecommenderApproaches(String sampleDir, String samplePath) {
		getTrainTestSize(samplePath);
		BookmarkReader reader = new BookmarkReader(TRAIN_SIZE, false);
		reader.readFile(samplePath);
		evaluate(sampleDir, samplePath, "apr", null, true, false, reader);
		evaluate(sampleDir, samplePath, "bll_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "bll_ac_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "bll_c_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "bll_c_ac_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "cf_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "fr", null, true, false, reader);
		evaluate(sampleDir, samplePath, "girp", null, true, false, reader);
		evaluate(sampleDir, samplePath, "girptm", null, true, false, reader);
		evaluate(sampleDir, samplePath, "layers_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "layerstagbll_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "layerstopicbll_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "lda_1000", null, true, false, reader);
		evaluate(sampleDir, samplePath, "mp", null, true, false, reader);
		evaluate(sampleDir, samplePath, "mp_r_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "mp_u_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "mp_ur_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "rescf_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "usercf_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "userlayers_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "userlayerstagbll_5_5", null, true, false, reader);
		evaluate(sampleDir, samplePath, "userlayerstopicbll_5_5", null, true, false, reader);
	}

	private static void startActCalculator(String sampleDir, String sampleName, int sampleCount, int dUpperBound, int betaUpperBound, boolean all, CalculationType type, boolean allMetrics) {
		getTrainTestSize(sampleName);
		List<Integer> dValues = getBetaValues(dUpperBound);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		String ac = type == CalculationType.USER_TO_RESOURCE ? "_ac" : "";
		BookmarkReader reader = null;
		
		for (int i = 1; i <= sampleCount; i++) {
			for (int dVal : dValues) {
				reader = BLLCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, false, dVal, 5, type);
				if (type == CalculationType.USER_TO_RESOURCE_ONLY) {
					writeMetrics(sampleDir, sampleName, "ac_5_5", sampleCount, 10, null, allMetrics ? reader : null, null);
				} else {
					writeMetrics(sampleDir, sampleName, "bll" + ac + "_" + 5 + "_" + dVal, sampleCount, 10, null, allMetrics ? reader : null, null);
				}
				if (all) {
					for (int betaVal : betaValues) {
						reader = BLLCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, true, dVal, betaVal, type);
						writeMetrics(sampleDir, sampleName, "bll_c" + ac + "_" + betaVal + "_" + dVal, sampleCount, 10, null, allMetrics ? reader : null, null);
					}
					//reader = BLLCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, false, true, dVal, 5, type);
					//writeMetrics(sampleDir, sampleName, "bll_r" + ac + "_" + 5 + "_" + dVal, sampleCount, 10, null, reader);
				}
			}
		}
	}

	private static void startGirpCalculator(String sampleDir, String sampleName, boolean all) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		reader = GIRPTMCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, false);
		writeMetrics(sampleDir, sampleName, "girp", 1, 10, null, reader, null);
		if (all) {
			reader = GIRPTMCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, true);
			writeMetrics(sampleDir, sampleName, "girptm", 1, 10, null, reader, null);
		}
	}
	
	private static void startRecCalculator(String sampleDir, String sampleName) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		reader = RecencyCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE);
		writeMetrics(sampleDir, sampleName, "rec", 1, 10, null, reader, null);
	}

	private static void startModelCalculator(String sampleDir, String sampleName, int sampleCount, int betaUpperBound, boolean all) {
		getTrainTestSize(sampleName);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		BookmarkReader reader = null;
		
		for (int i = 1; i <= sampleCount; i++) {
			reader = MPurCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, false, 5);
			if (all) reader = MPurCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, false, true, 5);
		}
		writeMetrics(sampleDir, sampleName, "mp_u_" + 5, sampleCount, 10, null, reader, null);
		if (all) writeMetrics(sampleDir, sampleName, "mp_r_" + 5, sampleCount, 10, null, reader, null);
		if (all) {
			for (int beta : betaValues) {
				for (int i = 1; i <= sampleCount; i++) {
					reader = MPurCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, true, beta);
				}
				writeMetrics(sampleDir, sampleName, "mp_ur_" + beta, sampleCount, 10, null, reader, null);
			}
		}
	}

	private static void startCfTagCalculator(String sampleDir, String sampleName, int sampleCount, int neighbors, int betaUpperBound, boolean all) {
		getTrainTestSize(sampleName);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		BookmarkReader reader = null;
		for (int i = 1; i <= sampleCount; i++) {
			reader = CFTagRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, true, false, 5);
			if (all) reader = CFTagRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, false, true, 5);
		}
		writeMetrics(sampleDir, sampleName, "usercf_" + 5, sampleCount, 10, null, reader, null);
		if (all) writeMetrics(sampleDir, sampleName, "rescf_" + 5, sampleCount, 10, null, reader, null);
		
		if (all) {
			for (int beta : betaValues) {
				for (int i = 1; i <= sampleCount; i++) {
					reader = CFTagRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, true, true, beta);
				}
				writeMetrics(sampleDir, sampleName, "cf_" + beta, sampleCount, 10, null, reader, null);
			}
		}
	}

	private static void startFolkRankCalculator(String sampleDir, String sampleName, int size) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			reader = FolkRankCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE);
		}
		writeMetrics(sampleDir, sampleName, "fr", size, 10, null, reader, null);
		writeMetrics(sampleDir, sampleName, "apr", size, 10, null, reader, null);
	}

	private static void startBaselineCalculator(String sampleDir, String sampleName, int size, boolean mp) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			reader = MPCalculator.predictPopularTags(sampleName, TRAIN_SIZE, TEST_SIZE, mp);
		}
		writeMetrics(sampleDir, sampleName, "mp", size, 10, null, reader, null);
	}

	private static void startLdaCalculator(String sampleDir, String sampleName, int topics, int sampleCount, boolean all) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		for (int i = 1; i <= sampleCount; i++) {
			reader = MalletCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, topics, true, all);
		}
		writeMetrics(sampleDir, sampleName, "lda_" + topics, sampleCount, 10, null, reader, null);
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
		
		BookmarkReader reader = null;		
		for (int i = 1; i <= size; i++) {
			for (int d : dValues) {
				if (resBased) {
					for (int b : betaValues) {
						reader = ThreeLTCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, d, b, true, true, tagBLL, topicBLL, CalculationType.NONE);
						writeMetrics(sampleDir, sampleName, suffix + "_" + b + "_" + d, size, 10, !topicString.isEmpty() ? topicString : null, reader, null);
					}
				}
				reader = ThreeLTCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, d, 5, true, false, tagBLL, topicBLL, CalculationType.NONE);
				writeMetrics(sampleDir, sampleName, "user" + suffix + "_" + 5 + "_" + d, size, 10, !topicString.isEmpty() ? topicString : null, reader, null);
			}
		}
	}
	
	private static void startContentBasedCalculator(String sampleDir, String sampleName) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = ContentBasedCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE);
		writeMetrics(sampleDir, sampleName, "cb", 1, 10, null, reader, null);
	}
	
	// Engine testing
	// --------------------------------------------------------------------------------------------------------------------------------------------------------------------
	private static void startEngineTest(String path, String filename) {
		EngineInterface recEngine = new EntityRecommenderEngine();
		try {
			recEngine.loadFile(path, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Tags for user and resource with BLLac+MPr: " + recEngine.getEntitiesWithLikelihood("http://sss.eu/302875752153271240/", "http://sss.eu/31116473812357491143/", null, 10, false, Algorithm.BLLacMPr, EntityType.TAG)); // BLLac+MPr
		System.out.println("Tags for user and resource with MPur: " + recEngine.getEntitiesWithLikelihood("http://sss.eu/302875752153271240/", "http://sss.eu/31116473812357491143/", null, 10, false, Algorithm.MPur, EntityType.TAG)); // MPur
		System.out.println("Tags with 3Lcoll: " + recEngine.getEntitiesWithLikelihood("http://sss.eu/302875752153271240/", "http://sss.eu/31116473812357491143/", null, 10, false, Algorithm.THREELcoll, EntityType.TAG)); // BLLac+MPr
		System.out.println("Tags with MP: " + recEngine.getEntitiesWithLikelihood("http://sss.eu/302875752153271240/", "http://sss.eu/31116473812357491143/", null, 10, false, Algorithm.MP, EntityType.TAG)); // MPur
		//System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("http://sss.eu/302875752153271240/", null, null, 10, false, null, EntityType.TAG)); // BLL
		//System.out.println("Tags for resource: " + recEngine.getEntitiesWithLikelihood(null, "http://sss.eu/31116473812357491143/", null, 10, false, null, EntityType.TAG)); // MPr
		//System.out.println("Tags MostPopular: " + recEngine.getEntitiesWithLikelihood(null, null, null, 10, false, null, EntityType.TAG)); // MP

		System.out.println("LD - Resources for user (Tags):" + recEngine.getEntitiesWithLikelihood("http://sss.eu/207091058105385/", null, null, 10, false, Algorithm.RESOURCETAGCB, EntityType.RESOURCE)); // CBtags
		System.out.println("Resources for user (CF): " + recEngine.getEntitiesWithLikelihood("http://sss.eu/207091058105385/", null, null, 10, false, null , EntityType.RESOURCE)); // CF
		System.out.println("Resources for resource: " + recEngine.getEntitiesWithLikelihood(null, "http://sss.eu/207091332279916/", null, 10, false, null, EntityType.RESOURCE)); // CF
		System.out.println("Resources MostPopular: " + recEngine.getEntitiesWithLikelihood(null, null, null, 10, false, null, EntityType.RESOURCE)); // MP

		System.out.println("LD - Users for resource (Tags): " + recEngine.getEntitiesWithLikelihood(null, "http://sss.eu/207091332279916/", null, 10, false, null , EntityType.USER)); // CBtags
		System.out.println("Users for user: " + recEngine.getEntitiesWithLikelihood("http://sss.eu/207091058105385/", null, null, 10, false, null, EntityType.USER)); // CF
		System.out.println("Users MostPopular: " + recEngine.getEntitiesWithLikelihood(null, null, null, 10, false, null, EntityType.USER)); // MP
	}
	
	private static void startKnowBrainTest(String path, String filename) {
		EngineInterface recEngine = new TagRecommenderEvalEngine();
		try {
			recEngine.loadFile(path, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// topics:t286,t40,t508 for 3Layers
		System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t3"), 10, false, Algorithm.THREELcoll, null));
		System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t848","t655","t448","t40","t53","t997","t508","t268"), 10, false, Algorithm.THREELcoll, null));
		System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t3"), 10, false, Algorithm.BLLcoll, null));
		System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t848","t655","t448","t40","t53","t997","t508","t268"), 10, false, Algorithm.BLLcoll, null));
		System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t848","t655","t448","t40","t53","t997","t508","t268"), 10, false, Algorithm.MP, null));
		//System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t3"), 10, false, Algorithm.THREEL, null));
		//System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t848","t655","t448","t40","t53","t997","t508","t268"), 10, false, Algorithm.THREEL, null));
		//System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t3"), 10, false, Algorithm.BLL, null));
		//System.out.println("Tags for user: " + recEngine.getEntitiesWithLikelihood("14", null, Arrays.asList("t848","t655","t448","t40","t53","t997","t508","t268"), 10, false, Algorithm.BLL, null));
	}
	
	// Helpers
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------
	private static void createLdaSamples(String sampleName, int size, int topics, boolean tagrec, boolean personalizedTopicCreation) {
		getTrainTestSize(sampleName);
		for (int i = 1; i <= size; i++) {
			MalletCalculator.createSample(sampleName, (short)topics, tagrec, TRAIN_SIZE, personalizedTopicCreation);			
		}
	}
	
	private static void writeTensorFiles(String sampleName, boolean tagRec) {
		getTrainTestSize(sampleName);
		CatDescFiltering filter = null;
		if (DESCRIBER != null) {
			filter = CatDescFiltering.instantiate(sampleName, TRAIN_SIZE);
			filter.setDescriber(DESCRIBER.booleanValue());
		}
		
		TensorProcessor.writeFiles(sampleName, TRAIN_SIZE, TEST_SIZE, tagRec, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, filter);
	}
	
	private static void writeMetrics(String sampleDir, String sampleName, String prefix, int sampleCount, int k, String posfix, BookmarkReader reader, Integer trainSize) {
		CatDescFiltering filter = null;
		if (DESCRIBER != null) {
			filter = CatDescFiltering.instantiate(sampleName, TRAIN_SIZE);
			filter.setDescriber(DESCRIBER.booleanValue());
		}
		
		String topicString = ((posfix == null || posfix == "0") ? "" : "_" + posfix);
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= sampleCount; j++) {
				MetricsCalculator.calculateMetrics(sampleName
						+ topicString + "_" + prefix, i, sampleDir + "/"
						+ prefix + topicString + "_metrics", false, reader, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, filter, true, trainSize);
			}
			MetricsCalculator.writeAverageMetrics(sampleDir + "/" + prefix + topicString + "_metrics", i, (double) sampleCount, true, i == k, DESCRIBER);
		}
		MetricsCalculator.resetMetrics();
	}

	// e.g., -5 will be transformed to 0.5 and 2 will be transformed to 0.1 and 0.2
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

	private static void getTrainTestStatistics(String dataset) {
		System.out.println("FULL SET -----");
		getStatistics(dataset, false);
		System.out.println("TRAIN SET -----");
		getStatistics(dataset + "_train", false);
		System.out.println("TEST SET -----");
		getStatistics(dataset + "_test", false);
	}
	
	private static void getStatistics(String dataset, boolean writeAll) {
		if (TOPIC_NAME != null) {
			dataset += ("_" + TOPIC_NAME);
		}
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(dataset);
		
		int bookmarks = reader.getBookmarks().size();
		System.out.println("Posts: " + bookmarks);
		int users = reader.getUsers().size();
		System.out.println("Users: " + users);
		int resources = reader.getResources().size();
		System.out.println("Resources: " + resources);
		int tags = reader.getTags().size();
		System.out.println("Tags: " + tags);
		int tagAssignments = reader.getTagAssignmentsCount();
		System.out.println("Tag-Assignments: " + tagAssignments);		
		int categories = reader.getCategories().size();
		System.out.println("Topics: " + categories);
		double avgTASPerPost = (double)tagAssignments / bookmarks;
		System.out.println("Avg. TAS per post: " + avgTASPerPost);
		double avgBookmarksPerUser = (double)bookmarks / users;
		System.out.println("Avg. resources/posts per user: " + avgBookmarksPerUser);
		double avgBookmarksPerResource = (double)bookmarks / resources;
		System.out.println("Avg. users/posts per resource: " + avgBookmarksPerResource);
		
		// TODO: write user distribution
		UserTagDistribution.calculate(reader, dataset);
		if (writeAll) {		
			try {
				getTrainTestSize(dataset);
				FileWriter userWriter = new FileWriter(new File("./data/metrics/" + dataset + "_userStats.txt"));
				BufferedWriter userBW = new BufferedWriter(userWriter);
				userBW.write("UserID| NoOfResources| NoOfTopics| Topic-Similarity\n");
				List<Bookmark> trainList = reader.getBookmarks().subList(0, TRAIN_SIZE);
				List<Integer> testUsers = reader.getUniqueUserListFromTestSet(TRAIN_SIZE);
				System.out.println();
	
				double avgTopicsPerUser = 0.0;
				double avgTopicDiversityPerUser = 0.0;
				List<Map<Integer, Double>> userTopics = Utilities.getRelativeTopicMaps(trainList, false);
				List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(trainList, false);
				for (int userID : testUsers) {
					Map<Integer, Double> topicsOfUser = userTopics.get(userID);
					double topicDiversityOfUser = Bookmark.getBookmarkDiversity(userBookmarks.get(userID));
					userBW.write(userID + "| " + reader.getUserCounts().get(userID) + "| " + topicsOfUser.keySet().size() + "| " + topicDiversityOfUser + "\n");
					avgTopicsPerUser += topicsOfUser.keySet().size();
					avgTopicDiversityPerUser += topicDiversityOfUser;
				}
				System.out.println("Avg. topics per user: " + avgTopicsPerUser / testUsers.size());
				System.out.println("Avg. topic-similarity per user: " + avgTopicDiversityPerUser / testUsers.size());
				double avgTopicsPerResource = Bookmark.getAvgNumberOfTopics(trainList);
				System.out.println("Avg. topics per resource: " + avgTopicsPerResource);
				userBW.flush();
				userBW.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		
		System.out.println();
	}

	private static void getTrainTestSize(String sample) {
		if (TOPIC_NAME != null) {
			sample += ("_" + TOPIC_NAME);
		}
		BookmarkReader trainReader = new BookmarkReader(-1, false);
		trainReader.readFile(sample + "_train");
		TRAIN_SIZE = trainReader.getBookmarks().size();
		System.out.println("Train-size: " + TRAIN_SIZE);
		BookmarkReader testReader = new BookmarkReader(-1, false);
		testReader.readFile(sample + "_test");
		TEST_SIZE = testReader.getBookmarks().size();
		System.out.println("Test-size: " + TEST_SIZE);
	}
	
	// passing the trainSize means that MyMediaLite files will be evaluated
	private static void evaluate(String sampleDir, String sampleName, String prefix, String postfix, boolean calcTags, boolean tensor, BookmarkReader reader) {
		if (reader == null) {
			getTrainTestSize(sampleName + (postfix != null ? "_" + postfix : ""));
			reader = new BookmarkReader(TRAIN_SIZE, false);
			reader.readFile(sampleName + (postfix != null ? "_" + postfix : ""));
		}
		if (calcTags) {
			writeMetrics(sampleDir, sampleName, prefix, 1, 10, postfix, reader,  tensor ? TRAIN_SIZE : null);
		} else {
			writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, postfix, reader, tensor ? TRAIN_SIZE : null);
		}
	}
	
	// Item Recommendation ------------------------------------------------------------------------------------------------------------------------------------	
	private static void startBaselineCalculatorForResources(String sampleDir, String sampleName, int size, boolean random) {
		BookmarkReader reader = null;
		String posfix = "";
		if (TOPIC_NAME != null) {
			posfix = "_" + TOPIC_NAME;
		}
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName + posfix);
			if (random) {
				reader = MPResourceCalculator.predictRandomResources(sampleName + posfix, TRAIN_SIZE);
			} else {
				reader = MPResourceCalculator.predictPopularResources(sampleName + posfix, TRAIN_SIZE);
			}
		}
		if (random) {
			writeMetricsForResources(sampleDir, sampleName, "rand", size, 20, TOPIC_NAME, reader, null);
		} else {
			writeMetricsForResources(sampleDir, sampleName, "pop", size, 20, TOPIC_NAME, reader, null);
		}
	}
	
	private static void startResourceCIRTTCalculator(String sampleDir, String sampleName, String topicString, int size, int neighborSize, Features features, 
			boolean userSim, boolean bll, boolean novelty, boolean calculateOnTag) {
		BookmarkReader reader = null;
		String posfix = "";
		if (TOPIC_NAME != null) {
			posfix = "_" + TOPIC_NAME;
		}
		String suffix = "r3l_" + features;
		if (bll) {
			suffix += "_bll";
		}
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName + posfix);
			reader = CIRTTCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, 
					neighborSize, features, userSim, bll, novelty, calculateOnTag);
		}
		writeMetricsForResources(sampleDir, sampleName, suffix, size, 20, !topicString.isEmpty() ? topicString : null, reader, null);
	}
	
	private static void startZhengResourceCalculator(String sampleDir, String sampleName, int size) {
		BookmarkReader reader = null;
		String posfix = "";
		if (TOPIC_NAME != null) {
			posfix = "_" + TOPIC_NAME;
		}
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName + posfix);
			reader = ZhengCalculator.predictSample(sampleName + posfix, TRAIN_SIZE);
		}
		writeMetricsForResources(sampleDir, sampleName, "zheng_tagtime", size, 20, TOPIC_NAME, reader, null);
	}
	
	private static void startHuangResourceCalculator(String sampleDir, String sampleName, int size) {
		BookmarkReader reader = null;
		String posfix = "";
		if (TOPIC_NAME != null) {
			posfix = "_" + TOPIC_NAME;
		}
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName + posfix);
			reader = HuangCalculator.predictSample(sampleName + posfix, TRAIN_SIZE);
		}
		writeMetricsForResources(sampleDir, sampleName, "huang_tag_user", size, 20, TOPIC_NAME, reader, null);
	}
	
	private static void startCfResourceCalculator(String sampleDir, String sampleName, int size, int neighborSize, boolean userBased, boolean resBased, boolean allResources, boolean bll, Features features) {
		BookmarkReader reader = null;
		String posfix = "";
		if (TOPIC_NAME != null) {
			posfix = "_" + TOPIC_NAME;
		}
		String suffix = "cf_";
		if (!userBased) {
			suffix = "rescf_";
		} else if (!resBased) {
			suffix = "usercf_";
		}
		if (!userBased && !allResources) {
			suffix += "mixed_";
		}
		if (bll) {
			suffix += "bll_";
		}
		//suffix += features + "_"; 
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName);
			reader = CFResourceCalculator.predictResources(sampleName + posfix, TRAIN_SIZE, TEST_SIZE, neighborSize, userBased, resBased, allResources, bll, features);
		}
		writeMetricsForResources(sampleDir, sampleName, suffix + "5", size, 20, TOPIC_NAME, reader, null);
	}
	
	private static void startSustainApproach(String sampleDir, String sampleName, double r, double tau, double beta, double learning_rate, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight) {
		BookmarkReader reader = null;
		getTrainTestSize(sampleName);
		SustainCalculator sustain = new SustainCalculator(sampleName, TRAIN_SIZE);
		//for (int i = 1; i <= size; i++) {
		reader = sustain.predictResources(r, tau, beta, learning_rate, trainingRecency, candidateNumber, sampleSize, cfWeight);
		//}
		String prefix = "sustain";
		writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, null, reader, TRAIN_SIZE);
	}
		
	private static void writeMetricsForResources(String sampleDir, String sampleName, String prefix, int sampleCount, int k, String posfix, BookmarkReader reader, Integer trainSize) {
		String topicString = ((posfix == null || posfix == "0") ? "_" : "_" + posfix + "_");
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= sampleCount; j++) {
				MetricsCalculator.calculateMetrics(sampleName + topicString + prefix, i, sampleDir + "/" + prefix + topicString + "_metrics", false, reader, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, null, false, trainSize);
			}
			MetricsCalculator.writeAverageMetrics(sampleDir + "/" + prefix + topicString + "metrics", i, (double)sampleCount, false, i == k, null);
		}
	}
}
