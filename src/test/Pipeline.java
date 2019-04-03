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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import common.Bookmark;
import common.CalculationType;
import common.Features;
import common.TimeUtil;
import common.Utilities;
import engine.Algorithm;
import engine.EngineInterface;
import engine.EntityRecommenderEngine;
import engine.EntityType;
import engine.TagRecommenderEvalEngine;
import file.BookmarkReader;
import file.BookmarkSplitter;
import file.postprocessing.CatDescFiltering;
import file.preprocessing.BibsonomyProcessor;
import file.preprocessing.CiteULikeProcessor;
import file.preprocessing.JKULFMProcessor;
import file.preprocessing.LastFMProcessor;
import file.preprocessing.MovielensProcessor;
import file.preprocessing.PintsProcessor;
import file.preprocessing.TensorProcessor;
import itemrecommendations.CFResourceCalculator;
import itemrecommendations.CIRTTCalculator;
import itemrecommendations.HuangCalculator;
import itemrecommendations.MPResourceCalculator;
import itemrecommendations.SustainCalculator;
import itemrecommendations.ZhengCalculator;
import processing.BLLCalculator;
import processing.CFTagRecommender;
import processing.ContentBasedCalculator;
import processing.FolkRankCalculator;
import processing.GIRPTMCalculator;
import processing.MPCalculator;
import processing.MPurCalculator;
import processing.MalletCalculator;
import processing.MetricsCalculator;
import processing.RecencyCalculator;
import processing.ThreeLTCalculator;
import processing.analyzing.TagReuseProbAnalyzer;
import processing.analyzing.UserTagDistribution;
import processing.hashtag.HashtagRecommendationEngine;
import processing.hashtag.analysis.ProcessFrequencyRecency;
import processing.hashtag.analysis.ProcessFrequencyRecencySocial;
import processing.hashtag.social.SocialStrengthCalculator;
import processing.hashtag.solr.CFSolrHashtagCalculator;
import processing.hashtag.solr.SolrHashtagCalculator;
import processing.hashtag.solr.Tweet;
import processing.musicrec.MusicCFRecommender;

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
    private final static String DATASET = "lfm1b";
    private final static String SUBDIR = "/low";// "general" / "researchers" for Twitter
    private static double dParam = 0.5;

    public static void main(String[] args) {
        System.out.println(
                "TagRecommender:\n" + "" + "A framework to implement and evaluate algorithms for the recommendation\n"
                        + "of tags. " + "Copyright (C) 2013 - 2019 Dominik Kowald\n\n"
                        + "This program is free software: you can redistribute it and/or modify\n"
                        + "it under the terms of the GNU Affero General Public License as published by\n"
                        + "the Free Software Foundation, either version 3 of the License, or\n"
                        + "(at your option) any later version.\n\n"
                        + "This program is distributed in the hope that it will be useful,\n"
                        + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                        + "GNU Affero General Public License for more details.\n\n"
                        + "You should have received a copy of the GNU Affero General Public License\n"
                        + "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
                        + "-----------------------------------------------------------------------------\n\n");
        
        String dir = DATASET + "_core" + SUBDIR + "/";
        String path = dir + DATASET + "_sample";
        String networkFileName = "./data/csv/" + dir + "network.txt";
        String solrServerNameWithPort = "http://kti-social:8938";
        Utilities.REC_LIMIT = 20;
        
        //JKULFMProcessor.preprocessFile("./data/schedl/high_main_users1000.txt", "./data/schedl/high_main_user_events_1000.txt");
        // Simone school dataset
        //BookmarkReader reader = null;//new BookmarkReader(0, false);
        //reader.readFile("school_core/school_core");
        //writeMetrics("school_core", "school_core/school", "MPI", 1, 5, null, reader, null);
        //writeMetrics("school_core", "school_core/school", "MPC", 1, 5, null, reader, null);
        //writeMetrics("school_core", "school_core/school", "BLLI", 1, 5, null, reader, null);
        //writeMetrics("school_core", "school_core/school", "BLLC", 1, 5, null, reader, null);
        //getStatistics("school_core/school_noRecs", false);
        
        ///////////////// test functions /////////////////////////////////////////////////////
        // --> dataset
        //getStatistics(path, false);
        
        // --> split dataset
        //BookmarkSplitter.splitSample(path, path, 1, 1, true, false, true, null, null);
        
        // --> analyze sample
        //analysisSocial(dir, path, null, "personal", TimeUtil.HOUR);
        
        // --> MP
        //startBaselineCalculator(dir, path, 1, true);
        
        // --> MPi
        //startModelCalculator(dir, path, 1, -5, false);
        
        // --> MRi
        //startRecCalculator(dir, path);
        
        // --> BLLi
        if (SUBDIR.contains("low")) {
        	dParam = 1.555;
        } else if (SUBDIR.contains("medium")) {
        	dParam = 1.599;
        } else {
        	dParam = 1.665;
        }
        //startActCalculator(dir, path, 1, dParam, null, -5, false, CalculationType.NONE, false); // artists
        //startActCalculator(dir, path, 1, 1.480, null, -5, false, CalculationType.NONE, false); // low genre
        //startActCalculator(dir, path, 1, 1.574, null, -5, false, CalculationType.NONE, false); // medium genre
        //startActCalculator(dir, path, 1, 1.587, null, -5, false, CalculationType.NONE, false); // high genre
        
        //int neighbors = 20;
        // --> CF_general
        //startCfMusicCalculator(dir, path, 1, neighbors, null, null, "general");
        // --> CF_pop
        //startCfMusicCalculator(dir, path, 1, neighbors, null, null, "pop");
        // --> CF_time
        //startCfMusicCalculator(dir, path, 1, neighbors, null, null, "time");
        
        // static BLL+CF
        //startCfMusicCalculator(dir, path, 1, 30, dParam, 0.5, "time");
        
        // dynamic BLL+CF
        //startCfMusicCalculator(dir, path, 1, 30, dParam, -1.0, "time");
               
        //startGirpCalculator(dir, path, false);
        
        // --> MPs
        //startSocialRecommendation(dir, path, networkFileName, "social_freq", 1.7, null, 1.25, null, null, null);

        // --> MRs
        //startSocialRecommendation(dir, path, networkFileName, "social_recency", 1.7, null, 1.25, null, null, null);
        
        // --> BLLi,s
        //startSocialRecommendation(dir, path, networkFileName, "hybrid", 1.7, null, 1.25, null, null, null);
        
        // --> SR (core names: "researcher" / "general")
        //startSolrHashtagCalculator(dir, path, solrServerNameWithPort, "general");
        
        // --> TCI
        //startSocialRecommendation(dir, path, networkFileName, "social_top_per_temp", 1.7, null, 1.25, null, solrServerNameWithPort, "general");
        
        // --> BLLi,s,c
        //startSocialRecommendation(dir, path, networkFileName, "hybrid", 1.7, null, 1.25, null, solrServerNameWithPort, "general");
        
        // Test the BLL_AC and BLL_AC+MP_r algorithms (could take a while)
        // startActCalculator(dir, path, 1, -5, -5, false,
        // CalculationType.USER_TO_RESOURCE_ONLY, false);
        // startActCalculator(dir, path, 1, 0.5, null, -5, true,
        // CalculationType.USER_TO_RESOURCE, false);

        // Test the GIRP and GIRPTM algorithms
        // startGirpCalculator(dir, path, true);

        // Test the CF_u, CF_r and CF_u_r algorithms with 20 neighbors (change it if you want)
        //startCfTagCalculator(dir, path, 1, 20, -5, false, true);

        // Test the PR and FR algorithms
        // startFolkRankCalculator(dir, path, 1);

        // Test the LDA algorithm with 1000 topics (change it if you want)
        // startLdaCalculator(dir, path, 1000, 1, false);

        // Test the 3L algorithm
        // start3LayersJavaCalculator(dir, path, "", 1, -5, -5, true, false, false);

        // Test the 3L_tag algorithm
        // start3LayersJavaCalculator(dir, path, "", 1, -5, -5, true, true, false);

        // Test the 3LT_topic algorithm
        // start3LayersJavaCalculator(dir, path, "", 1, -5, -5, true, false, true);

        // Test resource recommenders
        // MP
        //startBaselineCalculatorForResources(dir, path, 1, false, false);
        // CF
        //startCfResourceCalculator(dir, path, 1, 20, true, false, false, false, Features.ENTITIES, false);
        
        // Commandline Arguments
        if (args.length < 3) {
            System.out.println("Too few arguments!");
            return;
        }

        String subdir = "/";
        String op = args[0];
        String samplePath = "", sampleDir = "", sampleNetwork = "";
        int sampleCount = 1;

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
        } else if (args[1].equals("afel")) {
            sampleDir = "afel_core";
            subdir = "/res";
        } else {
            System.out.println("Dataset not available");
            return;
        }
        sampleDir += subdir;
        samplePath += (sampleDir + "/" + args[2]);
        sampleNetwork = "./data/csv/" + sampleDir + "/network.txt";

        boolean narrowFolksonomy = args[1].equals("flickr") || args[1].contains("twitter");
        if (op.equals("cf")) {
            startCfTagCalculator(sampleDir, samplePath, sampleCount, 20, -5, false, false);
        } else if (op.equals("cfr")) {
            startCfTagCalculator(sampleDir, samplePath, sampleCount, 20, -5, !narrowFolksonomy, false);
        } else if (op.equals("fr")) {
            startFolkRankCalculator(sampleDir, samplePath, sampleCount);
        } else if (op.equals("bll_c")) {
            startActCalculator(sampleDir, samplePath, sampleCount, 0.5, null, -5, !narrowFolksonomy,
                    CalculationType.NONE, true);
        } else if (op.equals("bll_c_ac")) {
            if (!narrowFolksonomy) {
                startActCalculator(sampleDir, samplePath, sampleCount, 0.5, null, -5, !narrowFolksonomy,
                        CalculationType.USER_TO_RESOURCE, true);
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
            BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 0, true, false, true, null, sampleNetwork);
        } else if (op.equals("split_8020")) {
            BookmarkSplitter.splitSample(samplePath, samplePath, sampleCount, 20, false, false, true, null, sampleNetwork);
        } else if (op.equals("percentage_sample")) {
            BookmarkSplitter.drawUserPercentageSample(samplePath, 3, 1);
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
            startBaselineCalculatorForResources(sampleDir, samplePath, sampleCount, false, false);
        } else if (op.equals("item_cft")) {
            startCfResourceCalculator(sampleDir, samplePath, sampleCount, 20, true, false, false, false, Features.TAGS, false);
        } else if (op.equals("item_cfb")) {
            startCfResourceCalculator(sampleDir, samplePath, sampleCount, 20, true, false, false, false, Features.ENTITIES, false);
        } else if (op.equals("item_cbt")) {
            //TOPIC_NAME = "lda_500";
            startCfResourceCalculator(sampleDir, samplePath, 1, 20, false, true, false, false, Features.TAGS, false);
        } else if (op.equals("item_zheng")) {
            startZhengResourceCalculator(sampleDir, samplePath, sampleCount);
        } else if (op.equals("item_huang")) {
            startHuangResourceCalculator(sampleDir, samplePath, sampleCount);
        } else if (op.equals("item_cirtt")) {
            startResourceCIRTTCalculator(sampleDir, samplePath, "", sampleCount, 20, Features.ENTITIES, false, true,
                    false, true);
        } else if (op.equals("item_sustain")) {
            startSustainApproach(sampleDir, samplePath, 2.845, 0.5, 6.396, 0.0936, 0, 0, 20, 0.5);
        } else if (op.equals("stats")) {
            try {
                getStatistics(samplePath, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (op.equals("hashtag_analysis")) {
            analysisSocial(sampleDir, samplePath, sampleNetwork, "all", TimeUtil.HOUR);
        } else if (op.equals("hashtag_hybrid")) {
            startSocialRecommendation(sampleDir, samplePath, sampleNetwork, "hybrid", 1.7, null, 1.25, null, null, null);
        } else if (op.equals("hashtag_socialmp")) {
            startSocialRecommendation(sampleDir, samplePath, sampleNetwork, "social_freq", 1.7, null, 1.25, null, null, null);
        } else if (op.equals("hashtag_socialbll")) {
            startSocialRecommendation(sampleDir, samplePath, sampleNetwork, "social", 1.7, null, 1.25, null, null, null);
        } else if (op.equals("hashtag_social_recency")) {
            startSocialRecommendation(sampleDir, samplePath, sampleNetwork, "social_recency", 1.7, null, 1.25, null,
                    null, null);
        } else if (op.equals("hashtag_cb_res")) {
            startSocialRecommendation(sampleDir, samplePath, sampleNetwork, "hybrid", 1.7, null, 1.25, null,
            		solrServerNameWithPort, "researcher");
        } else if (op.equals("hashtag_cb_gen")) {
            startSocialRecommendation(sampleDir, samplePath, sampleNetwork, "hybrid", 1.7, null, 1.25, null,
            		solrServerNameWithPort, "general");
        } else {
            System.out.println("Unknown operation");
        }
    }

	// Tag Recommenders methods
    // ---------------------------------------------------------------------------------------------------------------------------------------------
    private static void startSolrHashtagCalculator(String sampleDir, String samplePath, String solrUrl, String solrCore) {
        BookmarkReader reader = new BookmarkReader(0, false);
        reader.readFile(samplePath);
    	SolrHashtagCalculator.getNormalizedHashtagPredictions(sampleDir, solrCore, solrUrl, reader, null);
        writeMetrics(sampleDir, sampleDir + "/" + solrCore, "solrht_normalized", 1, 10, null, null, null);
    }

    private static void startAllTagRecommenderApproaches(String sampleDir, String samplePath, boolean all) {
        startBaselineCalculator(sampleDir, samplePath, 1, true); // MP
        startModelCalculator(sampleDir, samplePath, 1, -5, all); // MPur
        startGirpCalculator(sampleDir, samplePath, all); // GIRPTM
        startActCalculator(sampleDir, samplePath, 1, 0.5, null, -5, all, CalculationType.NONE, true); // BLL
        startActCalculator(sampleDir, samplePath, 1, 0.5, null, -5, all, CalculationType.USER_TO_RESOURCE, true); // BLLac
        start3LayersJavaCalculator(sampleDir, samplePath, "", 1, -5, -5, all, false, false); // 3L
        start3LayersJavaCalculator(sampleDir, samplePath, "", 1, -5, -5, all, true, false); // 3LTtop
        start3LayersJavaCalculator(sampleDir, samplePath, "", 1, -5, -5, all, false, true); // 3LTtag
        startCfTagCalculator(sampleDir, samplePath, 1, 20, -5, false, false); // CFur
        startFolkRankCalculator(sampleDir, samplePath, 1); // APR+FR
        startLdaCalculator(sampleDir, samplePath, 1000, 1, all); // LDA
    }

    private static void startSampleTagRecommenderApproaches(String sampleDir, String samplePath, boolean all) {
        startModelCalculator(sampleDir, samplePath, 1, -5, all); // MPur
        startGirpCalculator(sampleDir, samplePath, all); // GIRPTM
        startActCalculator(sampleDir, samplePath, 1, 0.5, null, -5, all, CalculationType.USER_TO_RESOURCE, false); // BLLac
        startFolkRankCalculator(sampleDir, samplePath, 1); // APR+FR
    }

    private static void startActCalculator(String sampleDir, String sampleName, int sampleCount, double dVal,
            Double lambda, int betaUpperBound, boolean all, CalculationType type, boolean allMetrics) {
        getTrainTestSize(sampleName);
        List<Integer> betaValues = getBetaValues(betaUpperBound);
        String ac = type == CalculationType.USER_TO_RESOURCE ? "_ac" : "";
        BookmarkReader reader = null;

        for (int i = 1; i <= sampleCount; i++) {
            reader = BLLCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, false, dVal, 5, type, lambda);
            if (type == CalculationType.USER_TO_RESOURCE_ONLY) {
                writeMetrics(sampleDir, sampleName, "ac_5_5", sampleCount, Utilities.REC_LIMIT, null, reader, null);
            } else {
                writeMetrics(sampleDir, sampleName, "bll" + ac + "_" + 5 + "_" + dVal, sampleCount, Utilities.REC_LIMIT, null, reader, null);
            }
            if (all) {
                for (int betaVal : betaValues) {
                    reader = BLLCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, true, dVal, betaVal, type, lambda);
                    writeMetrics(sampleDir, sampleName, "bll_c" + ac + "_" + betaVal + "_" + dVal, sampleCount, Utilities.REC_LIMIT, null, reader, null);
                }
            }
        }
    }

    private static void startSocialRecommendation(String sampleDir, String sampleName, String networkFilename,
            String algo, double dIndividual, Double lambdaIndividual, double dSocial, Double lambdaSocial,
            String solrUrl, String solrCore) {
        double betaBLL = 0.5;
        double betaCB = 0.3;

        String[] algos = null;
        if (algo == null) {
            algos = new String[] { "social_freq", "social_recency", "social", "hybrid", "social_link_weight", "social_top_per_temp" };
        } else {
            algos = new String[] { algo };
        }
        getTrainTestSize(sampleName);
        Map<Integer, Map<Integer, Double>> contentBasedValues = null;

        if (solrUrl != null && solrCore != null) {
            BookmarkReader reader = new BookmarkReader(0, false);
            reader.readFile(sampleName);
            if (new File("./data/results/" + sampleDir + "/" + solrCore + "_cbpredictions.ser").exists()) {
                System.out.println("Found cb file ...");
                contentBasedValues = SolrHashtagCalculator.deSerializeHashtagPrediction(
                        "./data/results/" + sampleDir + "/" + solrCore + "_cbpredictions.ser");
            } else {
                contentBasedValues = SolrHashtagCalculator.getNormalizedHashtagPredictions(sampleDir, solrCore, solrUrl,
                        reader, null);
                writeMetrics(sampleDir, sampleDir + "/" + solrCore, "solrht_normalized", 1, 10, null, null, null);
            }
            System.out.println("Number of content-based recommendations: " + contentBasedValues.size());
        }

        for (String a : algos) {
            System.out.println("Algorithm >> " + a);
            HashtagRecommendationEngine calculator = null;
            if ("social_link_weight".equals(a)) {
                String mentionFilename = "./data/csv/" + sampleDir + "/mentionNetwork.txt";
                String retweetFilename = "./data/csv/" + sampleDir + "/retweetNetwork.txt";
                String replyFilename = "./data/csv/" + sampleDir + "/replyNetwork.txt";
                System.out.println("Social init ... ");
                calculator = new HashtagRecommendationEngine(sampleDir, sampleName, networkFilename, mentionFilename,
                        retweetFilename, replyFilename, TRAIN_SIZE, TEST_SIZE, dIndividual, lambdaIndividual);
                System.out.println("Social init done ... ");
            } else if ("social_top_per_temp".equals(a)) {
                System.out.println("Solr Core >> " + solrCore);
                System.out.println("Solr Url >> " + solrUrl);
                calculator = new HashtagRecommendationEngine(sampleDir, sampleName, networkFilename, solrUrl, solrCore,
                        TRAIN_SIZE, TEST_SIZE, dIndividual, lambdaIndividual);
            } else if ("hybrid_link".equals(a)) {
                String mentionFilename = "./data/csv/" + sampleDir + "/mentionNetwork.txt";
                String retweetFilename = "./data/csv/" + sampleDir + "/retweetNetwork.txt";
                String replyFilename = "./data/csv/" + sampleDir + "/replyNetwork.txt";
                SocialStrengthCalculator socialStrengthCalculator = new SocialStrengthCalculator(mentionFilename,
                        retweetFilename, replyFilename);
                calculator = new HashtagRecommendationEngine(sampleDir, sampleName, networkFilename, TRAIN_SIZE,
                        TEST_SIZE, dIndividual, lambdaIndividual);
                calculator.setSocialStrengthCalculator(socialStrengthCalculator);
            } else {
                System.out.println("Social init ... ");
                calculator = new HashtagRecommendationEngine(sampleDir, sampleName, networkFilename, TRAIN_SIZE,
                        TEST_SIZE, dIndividual, lambdaIndividual);
                System.out.println("Social init done ... ");
            }

            if ("social_top_per_temp".equals(a)) {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(
                            "./data/results/" + sampleDir + "/social_top_per_temp_etah_etal_ndcg.txt", true));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                double eta_h = 0.1;
                double eta_l = 0.2;
                String suffix = "social_top_per_temp_" + eta_h + "_" + eta_l + "_" + a;
                System.out.println(" Pipeline >> eta_h " + new DecimalFormat("##.##").format(eta_h) + " >> eta_l >> "
                        + new DecimalFormat("##.##").format(eta_l));
                calculator.setEta_h(eta_h);
                calculator.setEta_l(eta_l);
                calculator.predictSample(betaBLL, betaCB, dSocial, lambdaSocial, a, null, suffix);
                writeMetrics(sampleDir, sampleName, suffix, 1, 10, null, null, null);
                double ndcg10 = MetricsCalculator.getNDCG10();
                String line = eta_h + ";" + eta_l + ";" + ndcg10 + "\n";
                System.out.println(" line >> " + line);
            } else {
                String suffix = "social" + betaCB + "_" + dSocial + "_" + a;
                calculator.predictSample(betaBLL, betaCB, dSocial, lambdaSocial, a, contentBasedValues, suffix);
                writeMetrics(sampleDir, sampleName, suffix, 1, 10, null, null, null);
                System.out.println("Algorithm done >> " + a);
            }
        }
    }

    private static void startCfCbHashtagCalculator(String sampleDir, String sampleName, double beta, String solrUrl, String solrCore) {
        getTrainTestSize(sampleName);
        CFSolrHashtagCalculator.predictSample(sampleDir, sampleName, TRAIN_SIZE, beta, solrUrl, solrCore);
        writeMetrics(sampleDir, sampleName, "cf_cb_" + beta, 1, 10, null, null, null);
    }

    private static void analysisSocial(String sampleDir, String sampleName, String networkFilename, String type,
            Integer granularity) {
        getTrainTestSize(sampleName);
        HashtagRecommendationEngine calculator = new HashtagRecommendationEngine(sampleDir, sampleName, networkFilename,
                TRAIN_SIZE, TEST_SIZE, 0.5, null);
        if (type.equals("social")) {
            new ProcessFrequencyRecencySocial(sampleDir, calculator.getUserTagTimes(), calculator.getNetwork(),
                    granularity);
        } else if (type.equals("personal")) {
            new ProcessFrequencyRecency().ProcessTagAnalytics(sampleDir, calculator.getUserTagTimes(), granularity);
        } else if (type.equals("all")) {
            new ProcessFrequencyRecency().ProcessTagAnalytics(sampleDir, calculator.getUserTagTimes(), granularity);
            new ProcessFrequencyRecencySocial(sampleDir, calculator.getUserTagTimes(), calculator.getNetwork(),
                    granularity);
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
    
    private static void startRecCalculator(String dir, String path) {
    	getTrainTestSize(path);
    	BookmarkReader reader = null;
    	reader = RecencyCalculator.predictSample(path, TRAIN_SIZE, TEST_SIZE);
    	writeMetrics(dir, path, "rec", 1, Utilities.REC_LIMIT, null, reader, null);
	}

    private static void startModelCalculator(String sampleDir, String sampleName, int sampleCount, int betaUpperBound,
            boolean all) {
        getTrainTestSize(sampleName);
        List<Integer> betaValues = getBetaValues(betaUpperBound);
        BookmarkReader reader = null;

        for (int i = 1; i <= sampleCount; i++) {
            reader = MPurCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, false, 5);
            if (all)
                reader = MPurCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, false, true, 5);
        }
        writeMetrics(sampleDir, sampleName, "mp_u_" + 5, sampleCount, Utilities.REC_LIMIT, null, reader, null);
        if (all)
            writeMetrics(sampleDir, sampleName, "mp_r_" + 5, sampleCount, Utilities.REC_LIMIT, null, reader, null);
        if (all) {
            for (int beta : betaValues) {
                for (int i = 1; i <= sampleCount; i++) {
                    reader = MPurCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, true, true, beta);
                }
                writeMetrics(sampleDir, sampleName, "mp_ur_" + beta, sampleCount, Utilities.REC_LIMIT, null, reader, null);
            }
        }
    }

    private static void startCfMusicCalculator(String sampleDir, String sampleName, int sampleCount, int neighbors, Double bllVal, Double beta, String type) {
        getTrainTestSize(sampleName);
        BookmarkReader reader = null;
        for (int i = 1; i <= sampleCount; i++) {
            reader = MusicCFRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, bllVal, beta, type);
        }
        String postfix = "cf_" + type + "_";
		if (beta != null) {
			postfix = "bll_cf_";
		}
        if (beta != null && beta > 0) {
        	postfix += "static_";
        }
        writeMetrics(sampleDir, sampleName, postfix + neighbors, sampleCount, Utilities.REC_LIMIT, null, reader, null);
    }
    
    private static void startCfTagCalculator(String sampleDir, String sampleName, int sampleCount, int neighbors,
            int betaUpperBound, boolean all, boolean ignoreResource) {
        getTrainTestSize(sampleName);
        List<Integer> betaValues = getBetaValues(betaUpperBound);
        BookmarkReader reader = null;
        for (int i = 1; i <= sampleCount; i++) {
            reader = CFTagRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, true, false, 5, ignoreResource);
            if (all)
                reader = CFTagRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, false, true, 5, ignoreResource);
        }
        writeMetrics(sampleDir, sampleName, "usercf_" + 5, sampleCount, 10, null, reader, null);
        if (all)
            writeMetrics(sampleDir, sampleName, "rescf_" + 5, sampleCount, 10, null, reader, null);

        if (all) {
            for (int beta : betaValues) {
                for (int i = 1; i <= sampleCount; i++) {
                    reader = CFTagRecommender.predictTags(sampleName, TRAIN_SIZE, TEST_SIZE, neighbors, true, true,
                            beta, ignoreResource);
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
        writeMetrics(sampleDir, sampleName, "mp", size, Utilities.REC_LIMIT, null, reader, null);
    }

    private static void startLdaCalculator(String sampleDir, String sampleName, int topics, int sampleCount,
            boolean all) {
        getTrainTestSize(sampleName);
        BookmarkReader reader = null;
        for (int i = 1; i <= sampleCount; i++) {
            reader = MalletCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE, topics, true, all);
        }
        writeMetrics(sampleDir, sampleName, "lda_" + topics, sampleCount, 10, null, reader, null);
    }

    private static void start3LayersJavaCalculator(String sampleDir, String sampleName, String topicString, int size,
            int dUpperBound, int betaUpperBound, boolean resBased, boolean tagBLL, boolean topicBLL) {
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
                        reader = ThreeLTCalculator.predictSample(
                                sampleName + (!topicString.isEmpty() ? "_" + topicString : ""), TRAIN_SIZE, TEST_SIZE,
                                d, b, true, true, tagBLL, topicBLL, CalculationType.NONE);
                        writeMetrics(sampleDir, sampleName, suffix + "_" + b + "_" + d, size, 10,
                                !topicString.isEmpty() ? topicString : null, reader, null);
                    }
                }
                reader = ThreeLTCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""),
                        TRAIN_SIZE, TEST_SIZE, d, 5, true, false, tagBLL, topicBLL, CalculationType.NONE);
                writeMetrics(sampleDir, sampleName, "user" + suffix + "_" + 5 + "_" + d, size, 10,
                        !topicString.isEmpty() ? topicString : null, reader, null);
            }
        }
    }

    private static void startContentBasedCalculator(String sampleDir, String sampleName) {
        getTrainTestSize(sampleName);
        BookmarkReader reader = ContentBasedCalculator.predictSample(sampleName, TRAIN_SIZE, TEST_SIZE);
        writeMetrics(sampleDir, sampleName, "cb", 1, 10, null, reader, null);
    }

    // Helpers
    // -----------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void createLdaSamples(String sampleName, int size, int topics, boolean tagrec,
            boolean personalizedTopicCreation) {
        getTrainTestSize(sampleName);
        for (int i = 1; i <= size; i++) {
            MalletCalculator.createSample(sampleName, (short) topics, tagrec, TRAIN_SIZE, personalizedTopicCreation);
        }
    }

    private static void writeTensorFiles(String sampleName, boolean tagRec) {
        getTrainTestSize(sampleName);
        CatDescFiltering filter = null;
        if (DESCRIBER != null) {
            filter = CatDescFiltering.instantiate(sampleName, TRAIN_SIZE);
            filter.setDescriber(DESCRIBER.booleanValue());
        }

        TensorProcessor.writeFiles(sampleName, TRAIN_SIZE, TEST_SIZE, tagRec, MIN_USER_BOOKMARKS, MAX_USER_BOOKMARKS,
                filter);
    }

    private static void writeMetrics(String sampleDir, String sampleName, String prefix, int sampleCount, int k,
            String posfix, BookmarkReader reader, Integer trainSize) {
        CatDescFiltering filter = null;
        if (DESCRIBER != null) {
            filter = CatDescFiltering.instantiate(sampleName, TRAIN_SIZE);
            filter.setDescriber(DESCRIBER.booleanValue());
        }

        String topicString = ((posfix == null || posfix == "0") ? "" : "_" + posfix);
        for (int i = 1; i <= k; i++) {
            for (int j = 1; j <= sampleCount; j++) {
                MetricsCalculator.calculateMetrics(sampleName + topicString + "_" + prefix, i,
                        sampleDir + "/" + prefix + topicString + "_metrics", false, reader, MIN_USER_BOOKMARKS,
                        MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, filter, true, trainSize);
            }
            MetricsCalculator.writeAverageMetrics(sampleDir + "/" + prefix + topicString + "_metrics", i,
                    (double) sampleCount, true, i == k, DESCRIBER);
        }
        MetricsCalculator.resetMetrics();

    }

    // e.g., -5 will be transformed to 0.5 and 2 will be transformed to 0.1 and
    // 0.2
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
        double avgTASPerPost = (double) tagAssignments / bookmarks;
        System.out.println("Avg. TAS per post: " + avgTASPerPost);
        double avgBookmarksPerUser = (double) bookmarks / users;
        System.out.println("Avg. resources/posts per user: " + avgBookmarksPerUser);
        double avgBookmarksPerResource = (double) bookmarks / resources;
        System.out.println("Avg. users/posts per resource: " + avgBookmarksPerResource);

        System.out.println("First timestamp: " + reader.getFirstTimestamp().toString());
        System.out.println("Last timestamp: " + reader.getLastTimestamp().toString());

        // write user distribution
        //UserTagDistribution.calculate(reader, dataset);
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
                    userBW.write(userID + "| " + reader.getUserCounts().get(userID) + "| "
                            + topicsOfUser.keySet().size() + "| " + topicDiversityOfUser + "\n");
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

    /**
     * 
     * Passing the trainSize means that MyMediaLite files will be evaluated
     * 
     * 
     */
    private static void evaluate(String sampleDir, String sampleName, String prefix, String postfix, boolean calcTags,
            boolean tensor, BookmarkReader reader) {
        if (reader == null) {
            getTrainTestSize(sampleName + (postfix != null ? "_" + postfix : ""));
            reader = new BookmarkReader(TRAIN_SIZE, false);
            reader.readFile(sampleName + (postfix != null ? "_" + postfix : ""));
        }
        if (calcTags) {
            writeMetrics(sampleDir, sampleName, prefix, 1, 10, postfix, reader, tensor ? TRAIN_SIZE : null);
        } else {
            writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, postfix, reader, tensor ? TRAIN_SIZE : null);
        }
    }

    // Item Recommendation
    // ------------------------------------------------------------------------------------------------------------------------------------
    private static void startBaselineCalculatorForResources(String sampleDir, String sampleName, int size,
            boolean random, boolean writeTime) {
        BookmarkReader reader = null;
        String posfix = "";
        if (TOPIC_NAME != null) {
            posfix = "_" + TOPIC_NAME;
        }
        for (int i = 1; i <= size; i++) {
            getTrainTestSize(sampleName + posfix);
            if (random) {
                reader = MPResourceCalculator.predictRandomResources(sampleName + posfix, TRAIN_SIZE, writeTime);
            } else {
                reader = MPResourceCalculator.predictPopularResources(sampleName + posfix, TRAIN_SIZE, writeTime);
            }
        }
        if (random) {
            writeMetricsForResources(sampleDir, sampleName, "rand", size, 20, TOPIC_NAME, reader, null);
        } else {
            writeMetricsForResources(sampleDir, sampleName, "mp", size, 20, TOPIC_NAME, reader, null);
        }
    }

    private static void startResourceCIRTTCalculator(String sampleDir, String sampleName, String topicString, int size,
            int neighborSize, Features features, boolean userSim, boolean bll, boolean novelty,
            boolean calculateOnTag) {
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
            reader = CIRTTCalculator.predictSample(sampleName + (!topicString.isEmpty() ? "_" + topicString : ""),
                    TRAIN_SIZE, TEST_SIZE, neighborSize, features, userSim, bll, novelty, calculateOnTag);
        }
        writeMetricsForResources(sampleDir, sampleName, suffix, size, 20, !topicString.isEmpty() ? topicString : null,
                reader, null);
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

    private static void startCfResourceCalculator(String sampleDir, String sampleName, int size, int neighborSize,
            boolean userBased, boolean resBased, boolean allResources, boolean bll, Features features,
            boolean writeTime) {
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
        suffix += features + "_";
        for (int i = 1; i <= size; i++) {
            getTrainTestSize(sampleName);
            reader = CFResourceCalculator.predictResources(sampleName + posfix, TRAIN_SIZE, TEST_SIZE, neighborSize,
                    userBased, resBased, allResources, bll, features, writeTime);
        }
        writeMetricsForResources(sampleDir, sampleName, suffix + "5", size, 20, TOPIC_NAME, reader, null);
    }

    private static void startSustainApproach(String sampleDir, String sampleName, double r, double tau, double beta,
            double learning_rate, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight) {
        BookmarkReader reader = null;
        getTrainTestSize(sampleName);
        SustainCalculator sustain = new SustainCalculator(sampleName, TRAIN_SIZE);

        reader = sustain.predictResources(r, tau, beta, learning_rate, trainingRecency, candidateNumber, sampleSize,
                cfWeight);

        String prefix = "sustain";
        writeMetricsForResources(sampleDir, sampleName, prefix, 1, 20, null, reader, TRAIN_SIZE);
    }

    private static void writeMetricsForResources(String sampleDir, String sampleName, String prefix, int sampleCount,
            int k, String posfix, BookmarkReader reader, Integer trainSize) {
        String topicString = ((posfix == null || posfix == "0") ? "_" : "_" + posfix + "_");
        for (int i = 1; i <= k; i++) {
            for (int j = 1; j <= sampleCount; j++) {
                MetricsCalculator.calculateMetrics(sampleName + topicString + prefix, i,
                        sampleDir + "/" + prefix + topicString + "_metrics", false, reader, MIN_USER_BOOKMARKS,
                        MAX_USER_BOOKMARKS, MIN_RESOURCE_BOOKMARKS, MAX_RESOURCE_BOOKMARKS, null, false, trainSize);
            }
            MetricsCalculator.writeAverageMetrics(sampleDir + "/" + prefix + topicString + "metrics", i,
                    (double) sampleCount, false, i == k, null);
        }
    }
}
