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

import itemrecommendations.HuangCalculator;
import itemrecommendations.NiemannApproach;
import itemrecommendations.Resource3LTCalculator;
import itemrecommendations.SustainApproach;
import itemrecommendations.ZhengCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import common.CalculationType;
import common.Features;
import processing.BLLCalculator;
import processing.BM25Calculator;
import processing.BaselineCalculator;
import processing.ContentBasedCalculator;
import processing.FolkRankCalculator;
import processing.LanguageModelCalculator;
import processing.MalletCalculator;
import processing.MetricsCalculator;
import processing.RecCalculator;
import processing.ThreeLayersCalculator;
import engine.BaseLevelLearningEngine;
import engine.CFResourceRecommenderEngine;
import engine.EngineInterface;
import engine.LanguageModelEngine;
import engine.TagRecommenderEngine;
import engine.ThreeLayersEngine;
import file.BookmarkReader;
import file.BookmarkSplitter;
import file.postprocessing.CatDescFiltering;
import file.preprocessing.TensorProcessor;

public class Pipeline {

	// are set automatically in code
	private static int TRAIN_SIZE;
	private static int TEST_SIZE;
	
	private static int DAY;
	private static int DURATION;
	
	// set for postprocessing (number of bookmarks - null is nothing)
	private final static Integer MIN_USER_BOOKMARKS = null;
	private final static Integer MAX_USER_BOOKMARKS = null;
	private final static Integer MIN_RESOURCE_BOOKMARKS = null;
	private final static Integer MAX_RESOURCE_BOOKMARKS = null;
	// set for categorizer/describer split (true is describer, false is categorizer - null for nothing)
	private final static Boolean DESCRIBER = null;

	//private final static String DATASET = "bib_core"; // placeholder for the used dataset
	//private final static String DATASET = "cul_core";
	//private final static String DATASET = "delicious";
	//private final static String DATASET = "movieLens";
//	private final static String DATASET = "lastFm";
	
	public static void main(String[] args) {
		System.out.println("TagRecommender:\n" + "" +
				"A framework to implement and evaluate algorithms for the recommendation\n" +
				"of tags." + 
			   	"Copyright (C) 2013 Dominik Kowald\n\n" + 
			   	"https://www.youtube.com/watch?v=MwpMEbgC7DA&list=RDH2-1u8xvk54&index=27  	"+"This program is free software: you can redistribute it and/or modify\n" + 
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
		// Resource-Recommender testing
	//	String dir = DATASET;
		
		List<Pair<String, String>> files = new LinkedList<Pair<String,String>>();
		files.add(new ImmutablePair<String,String>("travelWell", "travelWell" + "/" +"travelWell_sample"));
	//	files.add(new ImmutablePair<String,String>("bib_core", "bib_core" + "/" +"bib_sample_lda_500"));
	//files.add(new ImmutablePair<String,String>("cul_core", "cul_core" + "/" +"cul_sample_lda_500"));
	//files.add(new ImmutablePair<String,String>("cul_core", "cul_core" + "/" +"cul_sample_lda_500_param"));
	//files.add(new ImmutablePair<String,String>("delicious", "delicious" + "/" +"del_sample_lda_500"));
	//	files.add(new ImmutablePair<String,String>("delicious", "delicious" + "/" +"hetrec_sample_1_lda_24_res"));
	//	files.add(new ImmutablePair<String,String>("movieLens", "movieLens" + "/" +"ml_sample_lda_500"));
	//	files.add(new ImmutablePair<String,String>("lastFm", "lastFm"+ "/" +"lastfm_sample_lda_500"));
		
//		files.add(new ImmutablePair<String,String>("delicious", "delicious" + "/" +"hetrec_sample_1_lda_24_res"));

//		files.add(dir + "/" +"del_sample_lda_100");
//		files.add(dir + "/" +"del_sample_lda_500");
//		files.add(dir+ "/" +"lastfm_sample_lda_100");
		//files.add(dir+ "/" +"lastfm_sample_lda_500");
//		files.add(dir+ "/" +"ml_sample_lda_500");
		//String path = dir + "/" +"hetrec_sample";
		//String path = dir + "/" +"hetrec_sample_1_lda_24_res";
		//String path = dir + "/" +"del_sample_lda_100";
		//String path = dir + "/" +"del_sample_lda_500";
		//String path = dir + "/" +"hetrec_sample_1_lda_24_res_small";
		//getStatistics(path);
		//startCfResourceCalculator(dir, path, 1, 20, true, false, false, false, Features.ENTITIES);
		//startResourceCIRTTCalculator(dir, path, "", 1, 20, Features.ENTITIES, false, true, false, true);
//		startCfResourceCalculator(dir, path, 1, 20, true, false, false, false, Features.ENTITIES);
		//startResourceCIRTTCalculator("bib_core", "bib_core/bib_sample", "", 1, 20, Features.ENTITIES, false, true, false, true);
		//startBaselineCalculatorForResources(dir, path, 1, false);
		
		
		
		// initially beta was set to 1
		//beta from paper 6.396
		//double beta = 1;
		double beta =6.396;
		double [] betas = {6.396};
		// initially r was set to 2
		
		// r original paper = 9.998; 
		double r = 9.998; 
		//double [] rs = {2.0, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 3.0, 3.5, 4};
		
		//double [] learning_rates={0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		// first run
		//double [] rs = {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,9.1,9.2,9.3,9.4,9.5,9.6,9.7,9.8,9.9,9.91,9.92,9.93,9.94,9.95,9.96,9.97,9.98,9.99,9.991,9.992,9.993,9.994,	9.995,10.0};
		//double [] rs = {1.0,3.0,5.0,7.0,9.0,9.998,11.0,13.0,15.0,17.0,19.0,21.0};
		double [] rs = {9.998};
		//double [] rs = {19};
		//double [] rs = {9.998};
		// from paper: 0.096
		
		//first run
//		double [] learning_rates = new double[5];
//		learning_rates[4]= 0.096;
//		for (int i=6; i<=9; i++){
//			learning_rates[i-6] = Math.pow(10,(double)(i)/3.0-4.0);
//		}
		
		//double [] learning_rates={0.0936};
		//double [] learning_rates={1, 0.75, 0.5, 0.25, 0.1, 0.075, 0.05, 0.025, 0.01};
		//double [] learning_rates={1, 0.75, 0.5, 0.25, 0.1, 0.096, 0.075, 0.05, 0.025, 0.01, 0.0075, 0.005, 0.0025, 0.001, 0.00075};
		double [] learning_rates={0.096};
		//double[] gamma = {0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
		double[] gamma = {0.0};
		double learning_rate=0.1;
		//double learning_rate=0.0936;
		// tau according to paper = 0.5
		double tau_cluster=0.5;
		// first run
		double [] taus_cluster={0.5};
		//double [] taus_cluster={0.15};
		//double [] taus_cluster={0.5};
		// number of resources predicted for a user
		int sampleSize = 20;
		// number of resources considered for prediction prefiltered with CF
        int candidateNumber = 100;
		// number of recent resources considered for training
        int trainingRecency = 0;
        
        // E1: 0,10,20,30,40,50
        // final = 100
        int []candidateNumbers = {100}; 
        // E1: 0,5,10,20
        // E2: int []trainingRecencies = {0, 20, 25, 30};
        int []trainingRecencies = {0};
        double [] alpha = {0, 0.5};
  //      double [] alpha = {0.0, 0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1};
        //double [] CFWeights = {0.0,0.1, 0.2, 0.3, 0.4, 0.5, 0.6,0.7,0.8,0.9}; //best 0.35
        DAY = 0;
        
        
        for (Pair<String, String> file : files){
        	startNiemannApproach(file.getLeft(), file.getRight(), DAY);
        }
    /*    for (int cn =0; cn<candidateNumbers.length; cn++){
        	for (int tr=0; tr<trainingRecencies.length; tr++){
        		for (Pair<String, String> file : files){
        			for (int rc =0; rc<rs.length; rc++){
        				for (int lrc = 0; lrc<learning_rates.length; lrc++){
        					for (int cfc = 0; cfc<alpha.length; cfc++){
        						for (int tauc = 0; tauc<taus_cluster.length; tauc++){
        							for (int b = 0; b<betas.length; b++){
        								for (int gc = 0; gc<gamma.length; gc++){
        								startSustainApproach(file.getLeft(), file.getRight(), rs[rc], taus_cluster[tauc], betas[b], learning_rates[lrc], gamma[gc], trainingRecencies[tr], candidateNumbers[cn], sampleSize, alpha[cfc]);
        								System.out.println("r "+rs[rc]+"lr "+learning_rates[lrc]+"tau "+taus_cluster[tauc]);
        						//		startParameterEstimation(file.getLeft(), file.getRight(), rs[rc], taus_cluster[tauc], betas[b], learning_rates[lrc], gamma[gc],trainingRecencies[tr], candidateNumbers[cn], sampleSize, alpha[cfc]);
          						
        							}
        						}	
        					}	
        				}
        			}
        		}	
        	}	
        }*/
        //startSustainApproach(dir, path, r, tau_cluster, beta, learning_rate, trainingRecency, candidateNumber, sampleSize);
		//evaluate(dir, path, "sustain", null, false);
		
		
		// TODO: just execute to test your recommender - results can be found in metrics/bib_core
		//startContentBasedCalculator("bib_core", "bib_core/bib_sample");
		
		// Code example
		// Data-preprocessing: create p-core of level 3:
		//BookmarkSplitter.splitSample("bib_core/bib_dataset", "bib_core/bib_sample", 3);
		// Start algorithm: run BLL+C tag-recommender for the generated p-core sample:
		//BLLCalculator.predictSample("bib_core/bib_sample", TRAIN_SIZE, TEST_SIZE);
		// Evaluation and Post-Processing: evaluate the results for users and resources with a minimum/maximum number of bookmarks:
		//Pipeline.writeMetrics("bib_core", "bib_core/bib_sample", "bll_c", 10, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS);
		
		// Method Testing -> just uncomment the methods you want to test
		// Test the BLL and BLL+MP_r algorithms (= baseline to beat :))
		//startActCalculator("bib_core", "bib_core/bib_sample", 1, -5, -5, true, CalculationType.NONE);
		
		// Test the BLL_AC and BLL_AC+MP_r algorithms (could take a while)
		//startActCalculator("bib_core", "bib_core/bib_sample", 1, -5, -5, true, CalculationType.USER_TO_RESOURCE);
		
		// Test the GIRP and GIRPTM algorithms
		//startRecCalculator("bib_core", "bib_core/bib_sample");
		
		// Test the MP_u, MP_r and MP_u_r algorithms
		//startModelCalculator("bib_core", "bib_core/bib_sample", 1, -5);
		
		// Test the MP algorithm
		//startBaselineCalculator("bib_core", "bib_core/bib_sample", 1);
		
		// Test the CF_u, CF_r and CF_u_r algorithms with 20 neighbors (change it if you want)
		//startCfTagCalculator("bib_core", "bib_core/bib_sample", 1, 20, -5);
		
		// Test the PR and FR algorithms
		//startFolkRankCalculator("bib_core", "bib_core/bib_sample", 1);
		
		// Test the LDA algorithm with 100 topics (change it if you want)
		//startLdaCalculator("bib_core", "bib_core/bib_sample", 100, 1);
		
		// Test the 3L algorithm
		//start3LayersJavaCalculator("bib_core", "bib_core/bib_sample", "", 1, -5, -5, true, false, false);
		
		// Test the 3L_tag algorithm
		//start3LayersJavaCalculator("bib_core", "bib_core/bib_sample", "", 1, -5, -5, true, false, false);
		
		// Test the 3LT_topic algorithm
		//start3LayersJavaCalculator("bib_core", "bib_core/bib_sample", "", 1, -5, -5, true, false, false);
		
		// Engine Testing
		/*
		EngineInterface engine = new ThreeLayersEngine();
		try {
			engine.loadFile("bib_core/bib_sample" + "_1_lda_500_res");
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		System.out.println("3LT: " + engine.getEntitiesWithLikelihood("41", "545", Arrays.asList("ontology", "conference", "tutorial", "web2.0", "rss", "tools"), 10));
		BaseLevelLearningEngine bllEngine = new BaseLevelLearningEngine();
		try {
			bllEngine.loadFile("bib_core/bib_sample" + "_1_lda_500_res");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println("BLL: " + bllEngine.getEntitiesWithLikelihood("41", "545", null, 10));
		EngineInterface lmEngine = new LanguageModelEngine();
		try {
			lmEngine.loadFile("bib_core/bib_sample" + "_1_lda_500_res");
		} catch (Exception e3) {
			e3.printStackTrace();
		}
		System.out.println("LM: " + lmEngine.getEntitiesWithLikelihood("41", "545", null, 10));		
		EngineInterface tagrecEngine = new TagRecommenderEngine();
		try {
			tagrecEngine.loadFile("hugo2");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("TagRec with Topics: " + tagrecEngine.getEntitiesWithLikelihood("41", "545", Arrays.asList("ontology", "conference", "tutorial", "web2.0", "rss", "tools"), 10));
		System.out.println("TagRec without Topics: " + tagrecEngine.getEntitiesWithLikelihood("41", "545", null, 10));
		EngineInterface resrecEngine = new CFResourceRecommenderEngine();
		try {
			resrecEngine.loadFile("hugo2");
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		System.out.println("CF: " + resrecEngine.getEntitiesWithLikelihood("0", null, null, 10));
		*/
		
		// Commandline Arguments
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
		} else if (args[1].equals("ml")) {
			samplePath = "ml_core/" + args[2];
			sampleDir = "ml_core";
		} else if (args[1].equals("del")) {
			samplePath = "delicious/" + args[2];
			sampleDir = "delicious";
		} else {
			System.out.println("Dataset not available");
			return;
		}

		if (op.equals("cf")) {
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
			start3LayersJavaCalculator(sampleDir, samplePath, "lda_1000", sampleCount, -5, -5, true, false, false);
		} else if (op.equals("3LT")) {
			start3LayersJavaCalculator(sampleDir, samplePath, "lda_1000", sampleCount, -5, -5, true, true, false);
			start3LayersJavaCalculator(sampleDir, samplePath, "lda_1000", sampleCount, -5, -5, true, false, true);
		} else if (op.equals("lda")) {
			startLdaCalculator(sampleDir, samplePath, 1000, sampleCount);
		} else if (op.equals("lda_samples")) {
			createLdaSamples(samplePath, sampleCount, 1000);
		} else if (op.equals("tensor_samples")) {
			writeTensorFiles(samplePath, true);
		} else if (op.equals("core")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 3, 3, 3, false);
		} else if (op.equals("split_l1o")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 0, 0, 0, false);
		} else if (op.equals("split_8020")) {
			BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 0, 0, 0, true);		
		} else if (op.equals("item_mp")) {
			startBaselineCalculatorForResources(sampleDir, samplePath, sampleCount, false);
		} else if (op.equals("item_cft")) {
			boolean userBased = true, resourceBased = false, allResources  = false;
			startCfResourceCalculator(sampleDir, samplePath, sampleCount, 20, userBased, resourceBased, allResources, false, Features.TAGS);
		} else if (op.equals("item_cfb")) {
			boolean userBased = true, resourceBased = false, allResources  = false;
			startCfResourceCalculator(sampleDir, samplePath, sampleCount, 20, userBased, resourceBased, allResources, false, Features.ENTITIES);
		} else if (op.equals("item_zheng")) {
			startZhengResourceCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("item_huang")) {
			startHuangResourceCalculator(sampleDir, samplePath, sampleCount);
		} else if (op.equals("item_cirtt")) {
			boolean calculateUserSim = false, calculateBLL = true, calculateOnTag = true, novelty = false;
			startResourceCIRTTCalculator(sampleDir, samplePath, "", sampleCount, 20, Features.ENTITIES, calculateUserSim, calculateBLL, novelty, calculateOnTag);
		}else if (op.equals("sustain")) {
			if (args.length>3)
				trainingRecency = Integer.valueOf(args[3]);
			startSustainApproach(sampleDir, samplePath, r, tau_cluster, beta, learning_rate, trainingRecency, candidateNumber, sampleSize, 0.4);
		}else if (op.equals("sustainCF")) {
			if (args.length>3)
				trainingRecency = Integer.valueOf(args[3]);
			startSustainApproach(sampleDir, samplePath, r, tau_cluster, beta, learning_rate, trainingRecency, candidateNumber, sampleSize, 0.4);
		}}
//	}

	// helper methods ---------------------------------------------------------------------------------------------------------------------------------------------
	private static void startContentBasedCalculator(String sampleDir, String sampleName) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		ContentBasedCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE);
		writeMetrics(sampleDir, sampleName, "cb", 1, 10, null, reader);
	}
	
	
	
	
	private static void startActCalculator(String sampleDir, String sampleName,
			int sampleCount, int dUpperBound, int betaUpperBound, boolean all, CalculationType type) {
		getTrainTestSize(sampleName);
		List<Integer> dValues = getBetaValues(dUpperBound);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		String ac = type == CalculationType.USER_TO_RESOURCE ? "_ac" : "";
		BookmarkReader reader = null;
		
		for (int i = 1; i <= sampleCount; i++) {
			for (int dVal : dValues) {
				reader = BLLCalculator.predictSample(sampleName, TRAIN_SIZE,
						TEST_SIZE, true, false, dVal, 5, type);
				writeMetrics(sampleDir, sampleName,
						"bll" + ac + "_" + 5 + "_" + dVal, sampleCount, 10, null, reader);
				if (all) {
					for (int betaVal : betaValues) {
						reader = BLLCalculator.predictSample(sampleName,
								TRAIN_SIZE, TEST_SIZE, true, true, dVal,
								betaVal, type);
						writeMetrics(sampleDir, sampleName, "bll_c" + ac + "_" + betaVal
								+ "_" + dVal, sampleCount, 10, null, reader);
					}
					reader = BLLCalculator.predictSample(sampleName,
							TRAIN_SIZE, TEST_SIZE, false, true, dVal, 5, type);
					writeMetrics(sampleDir, sampleName, "bll_r" + ac + "_" + 5 + "_"
							+ dVal, sampleCount, 10, null, reader);
				}
			}
		}
	}

	// passing the trainSize means that MyMediaLite files will be evaluated
	private static void evaluate(String sampleDir, String sampleName, String prefix, String postfix, boolean calcTags) {
	getTrainTestSize(sampleName);
	BookmarkReader reader = new BookmarkReader(TRAIN_SIZE, false);
	reader.readFile(sampleName);
	if (calcTags) {
	writeMetrics(sampleDir, sampleName, prefix, 1, 10, postfix, reader);
	} else {
		writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, postfix, reader);
		}
	}
	
	private static void startRecCalculator(String sampleDir, String sampleName) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		RecCalculator.predictSample(sampleName, TRAIN_SIZE,
				TEST_SIZE, true, false);
		writeMetrics(sampleDir, sampleName, "girp", 1, 10, null, reader);
		RecCalculator.predictSample(sampleName, TRAIN_SIZE,
				TEST_SIZE, true, true);
		writeMetrics(sampleDir, sampleName, "girptm", 1, 10, null, reader);
	}

	private static void startModelCalculator(String sampleDir,
			String sampleName, int sampleCount, int betaUpperBound) {
		getTrainTestSize(sampleName);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		BookmarkReader reader = null;
		
		for (int i = 1; i <= sampleCount; i++) {
			LanguageModelCalculator.predictSample(sampleName,
					TRAIN_SIZE, TEST_SIZE, true, false, 5);
			LanguageModelCalculator.predictSample(sampleName,
					TRAIN_SIZE, TEST_SIZE, false, true, 5);
		}
		writeMetrics(sampleDir, sampleName, "mp_u_" + 5, sampleCount, 10, null, reader);
		writeMetrics(sampleDir, sampleName, "mp_r_" + 5, sampleCount, 10, null, reader);
		for (int beta : betaValues) {
			for (int i = 1; i <= sampleCount; i++) {
				LanguageModelCalculator.predictSample(sampleName,
						TRAIN_SIZE, TEST_SIZE, true, true, beta);
			}
			writeMetrics(sampleDir, sampleName, "mp_ur_" + beta, sampleCount, 10, null, reader);
		}
	}

	private static void startCfTagCalculator(String sampleDir, String sampleName, int sampleCount, int neighbors, int betaUpperBound) {
		getTrainTestSize(sampleName);
		List<Integer> betaValues = getBetaValues(betaUpperBound);
		BookmarkReader reader = null;
		for (int i = 1; i <= sampleCount; i++) {
			reader = BM25Calculator.predictTags(sampleName, TRAIN_SIZE,
					TEST_SIZE, neighbors, true, false, 5);
			reader = BM25Calculator.predictTags(sampleName, TRAIN_SIZE,
					TEST_SIZE, neighbors, false, true, 5);
		}
		writeMetrics(sampleDir, sampleName, "usercf_" + 5, sampleCount, 10, null, reader);
		writeMetrics(sampleDir, sampleName, "rescf_" + 5, sampleCount, 10, null, reader);
		for (int beta : betaValues) {
			for (int i = 1; i <= sampleCount; i++) {
				reader = BM25Calculator.predictTags(sampleName, TRAIN_SIZE,
						TEST_SIZE, neighbors, true, true, beta);
			}
			writeMetrics(sampleDir, sampleName, "cf_" + beta, sampleCount, 10, null, reader);
		}
	}

	private static void startFolkRankCalculator(String sampleDir,
			String sampleName, int size) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			FolkRankCalculator.predictSample(sampleName, TRAIN_SIZE,
					TEST_SIZE, true);
		}
		writeMetrics(sampleDir, sampleName, "fr", size, 10, null, reader);
		writeMetrics(sampleDir, sampleName, "apr", size, 10, null, reader);
	}

	private static void startBaselineCalculator(String sampleDir,
			String sampleName, int size) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			BaselineCalculator.predictPopularTags(sampleName,
					TRAIN_SIZE, TEST_SIZE);
		}
		writeMetrics(sampleDir, sampleName, "mp", size, 10, null, reader);
	}

	private static void startLdaCalculator(String sampleDir, String sampleName, int topics, int sampleCount) {
		getTrainTestSize(sampleName);
		BookmarkReader reader = null;
		for (int i = 1; i <= sampleCount; i++) {
			MalletCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, topics, true, true);
		}
		writeMetrics(sampleDir, sampleName, "lda_" + topics, sampleCount, 10, null, reader);
	}
	
	private static void createLdaSamples(String sampleName, int size, int topics) {
		getTrainTestSize(sampleName);
		for (int i = 1; i <= size; i++) {
			MalletCalculator.createSample(sampleName, TEST_SIZE, (short)topics, false, true);			
		}
	}
	
	private static void writeTensorFiles(String sampleName, boolean tagRec) {
		getTrainTestSize(sampleName);
		CatDescFiltering filter = null;
		if (DESCRIBER != null) {
			filter = CatDescFiltering.instantiate(sampleName, TRAIN_SIZE);
			filter.setDescriber(DESCRIBER.booleanValue());
		}
		
		TensorProcessor.writeFiles(sampleName + "_" + 1, TRAIN_SIZE, TEST_SIZE, tagRec, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, filter);
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
						reader = ThreeLayersCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, d, b, true, true, tagBLL, topicBLL);
						writeMetrics(sampleDir, sampleName, suffix + "_" + b + "_" + d, size, 10, !topicString.isEmpty() ? topicString : null, reader);
					}
				}
				reader = ThreeLayersCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, d, 5, true, false, tagBLL, topicBLL);
				writeMetrics(sampleDir, sampleName, "user" + suffix + "_" + 5 + "_" + d, size, 10, !topicString.isEmpty() ? topicString : null, reader);
			}
		}
	}
	
	// Helpers
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------
	private static void writeMetrics(String sampleDir, String sampleName, String prefix, int k, Integer minUserBookmarks, Integer maxUserBookmarks, Integer minResourceBookmarks, Integer maxResourceBookmarks) {
		writeMetrics(sampleDir, sampleName, prefix, 1, k, null, null);
	}
	
	private static void writeMetrics(String sampleDir, String sampleName, String prefix, int sampleCount, int k, String posfix, BookmarkReader reader) {
		CatDescFiltering filter = null;
		if (DESCRIBER != null) {
			//getTrainTestSize(sampleName + "_1");
			filter = CatDescFiltering.instantiate(sampleName, TRAIN_SIZE);
			filter.setDescriber(DESCRIBER.booleanValue());
		}
		
		String topicString = ((posfix == null || posfix == "0") ? "" : "_" + posfix);
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= sampleCount; j++) {
				MetricsCalculator.calculateMetrics(sampleName
						+ topicString + "_" + prefix, i, sampleDir + "/"
						+ prefix + topicString + "_metrics", false, reader, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, filter, true);
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
	
	// Item Recommendation ------------------------------------------------------------------------------------------------------------------------------------	
	// MP or Random Algorithm
	private static void startBaselineCalculatorForResources(String sampleDir, String sampleName, int size, boolean random) {
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName);
			if (random) {
				reader = BaselineCalculator.predictRandomResources(sampleName, TRAIN_SIZE);
			} else {
				reader = BaselineCalculator.predictPopularResources(sampleName, TRAIN_SIZE);
			}
		}
		if (random) {
			writeMetricsForResources(sampleDir, sampleName, "rand", size, 20, null, reader);
		} else {
			writeMetricsForResources(sampleDir, sampleName, "pop", size, 20, null, reader);
		}
	}
	
	private static void startResourceCIRTTCalculator(String sampleDir, String sampleName, String topicString, int size, int neighborSize, Features features, 
			boolean userSim, boolean bll, boolean novelty, boolean calculateOnTag) {
		BookmarkReader reader = null;
		String suffix = "r3l_" + features;
		if (bll) {
			suffix += "_bll";
		}
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName);
			reader = Resource3LTCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE, 
					neighborSize, features, userSim, bll, novelty, calculateOnTag);
		}
		writeMetricsForResources(sampleDir, sampleName, suffix, size, 20, !topicString.isEmpty() ? topicString : null, reader);
	}
	
	private static void startZhengResourceCalculator(String sampleDir, String sampleName, int size) {
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName);
			reader = ZhengCalculator.predictSample(sampleName, TRAIN_SIZE);
		}
		writeMetricsForResources(sampleDir, sampleName, "zheng_tagtime", size, 20, null, reader);

	}
	
	
	private static void startHuangResourceCalculator(String sampleDir, String sampleName, int size) {
		BookmarkReader reader = null;
		for (int i = 1; i <= size; i++) {
			getTrainTestSize(sampleName);
			reader = HuangCalculator.predictSample(sampleName, TRAIN_SIZE);
		}
		writeMetricsForResources(sampleDir, sampleName, "huang_tag_user", size, 20, null, reader);
	}
	
	private static void startCfResourceCalculator(String sampleDir, String sampleName, int size, int neighborSize, boolean userBased, boolean resBased, boolean allResources, boolean bll, Features features) {
		BookmarkReader reader = null;
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
			reader = BM25Calculator.predictResources(sampleName, TRAIN_SIZE, TEST_SIZE, neighborSize, userBased, resBased, allResources, bll, features);
		}
		writeMetricsForResources(sampleDir, sampleName, suffix + "5", size, 20, null, reader);
	}
	
	private static void startParameterEstimation(String sampleDir, String sampleName, double r, double tau, double beta, double learning_rate, double gamma, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight) {
		BookmarkReader reader = null;
		getTrainTestSize(sampleName);
		SustainApproach sustain = new SustainApproach(sampleName, TRAIN_SIZE);
		reader = sustain.predictResources(r, tau, beta, learning_rate, gamma,trainingRecency, candidateNumber, sampleSize, cfWeight);
				
		String dir = sampleDir + "/" + "sust_param" + "_" + "metrics";
		String inputFile = sampleName + "_" + "sustain";
		String outputFile = sampleName + "_" + "sust_param";
		MetricsCalculator.calculateNDCGMetrics(inputFile, 20, dir, reader, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, null, r, tau, learning_rate, gamma, cfWeight, sustain.getMinCluster(), sustain.getMaxCluster(), sustain.getMinResources(), sustain.getMaxResources());
	}
	
	
	private static void startSustainApproach(String sampleDir, String sampleName, double r, double tau, double beta, double learning_rate, double gamma, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight) {
		BookmarkReader reader = null;
		getTrainTestSize(sampleName);
		SustainApproach sustain = new SustainApproach(sampleName, TRAIN_SIZE);
		//for (int i = 1; i <= size; i++) {
		reader = sustain.predictResources(r, tau, beta, learning_rate, gamma,trainingRecency, candidateNumber, sampleSize, cfWeight);
		//}
		
		// todo check whether size is needed as a parameter
		//writeMetricsForResources(sampleDir, sampleName, "sustain", size, 20, null, reader);
		String prefix = "sustain";
		writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, null, reader);
	}
	
	
	private static void startNiemannApproach(String sampleDir, String sampleName, int sessionIteration) {
		BookmarkReader reader = null;
		getTrainTestSize(sampleName);
		NiemannApproach niemann = new NiemannApproach(sampleName, TRAIN_SIZE);
			reader = niemann.predictResources(sessionIteration, "Europe/Vienna");
		String prefix = "niemann";
		writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, null, reader);
	}
	
	//FIXME: uncomment predictResources
	private static void startSustainApproach(String sampleDir, String sampleName, double r, double tau, double beta, double learning_rate, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight) {
		BookmarkReader reader = null;
		getTrainTestSize(sampleName);
		SustainApproach sustain = new SustainApproach(sampleName, TRAIN_SIZE);
//		for (int i = 1; i <= size; i++) {
//			reader = sustain.predictResources(r, tau, beta, learning_rate, trainingRecency, candidateNumber, sampleSize, cfWeight);
//		}
//		
		// todo check whether size is needed as a parameter
		//writeMetricsForResources(sampleDir, sampleName, "sustain", size, 20, null, reader);
		String prefix = "sustain";
		writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, null, reader);
	}
	
	private static void writeMetricsForResources(String sampleDir, String sampleName, String prefix, int sampleCount, int k, String posfix, BookmarkReader reader) {
		String topicString = ((posfix == null || posfix == "0") ? "_" : "_" + posfix);
		
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= sampleCount; j++) {
				MetricsCalculator.calculateMetrics(sampleName + topicString + prefix, i, sampleDir + "/" + prefix + topicString + "_metrics", false, reader, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, null, false);
			}
			
			MetricsCalculator.writeAverageMetrics(sampleDir + "/" + prefix + topicString + "metrics", i, (double)sampleCount, false, i == k, null);
		}
	}
}
