package processing.hashtag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Ints;

import common.Bookmark;
import common.MapUtil;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;
import processing.BLLCalculator;
import processing.hashtag.baseline.ContentPersonalTemporalCalculator;
import processing.hashtag.social.SocialBLLCalculator;
import processing.hashtag.social.SocialFrequencyCalculator;
import processing.hashtag.social.SocialHybridCalculator;
import processing.hashtag.social.SocialInitEngine;
import processing.hashtag.social.SocialLinkWeightCalculator;
import processing.hashtag.social.SocialRecencyRecommender;
import processing.hashtag.social.SocialStrengthCalculator;
import processing.hashtag.solr.SolrHashtagCalculator;

/**
 * @author spujari
 *
 */
public class HashtagRecommendationEngine {

	private BookmarkReader reader;
	private String filename;
	private String sampleDir;
	private HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes;
	private HashMap<String, ArrayList<String>> network;
	private List<String> users;
	private List<String> tags;
	private int trainSize;
	private List<String> idNameMap;
	private List<Map<Integer, Double>> resultMapPersonalBLLAllUsers;
	private List<Map<Integer, Double>> resultMapPersonalFreqAllUsers;
	private String mentionFilename;
	private String retweetFilename;
	private String replyFilename;
	private String solrCore;
	private String solrUrl;
	private double eta_h;
	private double eta_l;
	private SocialStrengthCalculator socialStrengthCalculator;

    /**
	 * Base constructor.
	 * 
	 * @param sampleDir
	 * @param userTweetFilename
	 * @param networkFilename
	 * @param trainSize
	 * @param testSize
	 * @param dIndividual
	 * @param lambdaIndividual
	 */
	public HashtagRecommendationEngine(String sampleDir, String userTweetFilename, String networkFilename, int trainSize,
			int testSize, double dIndividual, Double lambdaIndividual) {
		this.filename = userTweetFilename;
		this.sampleDir = sampleDir;
		this.trainSize = trainSize;
		initUserTagTime(userTweetFilename, trainSize);
		if (networkFilename != null) {
			initNetwork(networkFilename);
		}
		initBLLAndFreqMaps(trainSize, dIndividual, lambdaIndividual);
	}
	
	/**
	 * 
	 * @param sampleDir
	 * @param userTweetFilename
	 * @param networkFilename
	 * @param mentionFilename
	 * @param retweetFilename
	 * @param replyFilename
	 * @param trainSize
	 * @param testSize
	 * @param dIndividual
	 * @param lambdaIndividual
	 **/
	public HashtagRecommendationEngine(String sampleDir, String userTweetFilename, String networkFilename, String mentionFilename,
			String retweetFilename, String replyFilename, int trainSize, int testSize, double dIndividual,
			Double lambdaIndividual) {
		this(sampleDir, userTweetFilename, networkFilename, trainSize, testSize, dIndividual, lambdaIndividual);
		this.mentionFilename = mentionFilename;
		this.retweetFilename = retweetFilename;
		this.replyFilename = replyFilename;
	}
	
	/**
	 * 
	 * @param sampleDir
	 * @param userTweetFilename
	 * @param networkFilename
	 * @param solrUrl
	 * @param solrCore
	 * @param trainSize
	 * @param testSize
	 * @param dIndividual
	 * @param lambdaIndividual
	 */
	public HashtagRecommendationEngine(String sampleDir, String userTweetFilename, String networkFilename, String solrUrl, String solrCore,
			int trainSize, int testSize, double dIndividual, Double lambdaIndividual){
		this(sampleDir, userTweetFilename, networkFilename, trainSize, testSize, dIndividual, lambdaIndividual);
		this.solrCore = solrCore;
		this.solrUrl = solrUrl;
	}

	/**
	 * Init Network.
	 * 
	 **/
	private void initNetwork(String networkFilename) {
		this.network = SocialInitEngine.getNetwork(networkFilename, SocialInitEngine.getNameIdMap(idNameMap));
	}

	/**
	 * Init UserTagTime Hashmap.
	 **/
	private void initUserTagTime(String userTweetFilename, int trainSize) {
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(userTweetFilename);
		this.users = reader.getUsers();
		this.tags = reader.getTags();
		this.idNameMap = reader.getUsers();
		this.userTagTimes = SocialInitEngine.getBookmarks(reader.getBookmarks().subList(0, this.trainSize), users);
	}

	/**
	 * Init BLL and Freq score maps which are later used for the hybrid approaches.
	 **/
	private void initBLLAndFreqMaps(int trainSize, double dIndividual, Double lambdaIndividual) {
		List<Bookmark> trainList = this.reader.getBookmarks().subList(0, this.trainSize);
		List<Bookmark> testList = this.reader.getBookmarks().subList(this.trainSize, this.reader.getBookmarks().size());
		this.resultMapPersonalBLLAllUsers = BLLCalculator.getArtifactMaps(reader, trainList, testList, false,
				new ArrayList<Long>(), new ArrayList<Double>(), dIndividual, true, lambdaIndividual, true);
		this.resultMapPersonalFreqAllUsers = Utilities
				.getNormalizedMaps(this.reader.getBookmarks().subList(0, trainSize), false);
	}
	
	/**
	 * Get User Tag times
	 * 
	 * @return Hashmap of user-user timestamps
	 */
	public HashMap<String, HashMap<Integer, ArrayList<Long>>> getUserTagTimes() {
		return userTagTimes;
	}

	/**
	 * Get the underlying network.
	 * 
	 * @return Network
	 */
	public HashMap<String, ArrayList<String>> getNetwork() {
		return this.network;
	}

	/**
	 * For all test data create the map of tags to the weight.
	 * 
	 * @param beta
	 * @param exponentSocial
	 * @param algorithm
	 *            The algorithm for creating the recommendation list.
	 * @param sort
	 *            Whether the map is to be sorted.
	 * @return List of Map where is item represents a set of tags and score
	 *         assigned to them.
	 */
	private List<Map<Integer, Double>> calculateSocialTagScore(double beta, double exponentSocial, String algorithm,
			boolean sort) {
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		if (algorithm.equals("social_top_per_temp")){
			ContentPersonalTemporalCalculator contentPersTempCalculator = null;
			Map<Integer, Map<Integer, Double>> contentBasedValues = new HashMap<Integer, Map<Integer, Double>>();
			String serialFilePath = "./data/results/" + sampleDir + "/" + solrCore + "_cbpredictions.ser";
			if(new File(serialFilePath).exists()){
				contentBasedValues = SolrHashtagCalculator.deSerializeHashtagPrediction(serialFilePath);
				System.out.println(">> serial file path exist >> " + serialFilePath);
				contentPersTempCalculator = new ContentPersonalTemporalCalculator(userTagTimes, network, users, tags, solrUrl, solrCore, sampleDir, contentBasedValues);
				contentPersTempCalculator.setEta_h(eta_h);
				contentPersTempCalculator.setEta_l(eta_l);
				System.out.println(" HashtagRecommendation >> eta_h " + eta_h + " >> eta_l >> " + eta_l);
				for (int i = trainSize; i < reader.getBookmarks().size(); i++) {
					Bookmark data = reader.getBookmarks().get(i);
					Map<Integer, Double> map = new HashMap<Integer, Double>();
					// compute score only if the text based similarity metric is available.
					if(contentBasedValues.containsKey(data.getUserID())){
					    map = contentPersTempCalculator.getSimilarityScoreVersion3(data.getUserID(), data.getTimestampAsLong(), true);
					    //System.out.println("sorted map >>" + map);
					    results.add(map);
					}else{
					    //System.out.println("user id not present ");
					    results.add(null);
					}
				}
			}else{
				System.out.println(">> serial file path not exist >> " + serialFilePath);
			}
			return results;
		}if(algorithm.equals("social_link_weight")){
			SocialLinkWeightCalculator socialLinkWeightCalculator = new SocialLinkWeightCalculator(userTagTimes, network, users, mentionFilename, retweetFilename,
					replyFilename);
			for (int i = trainSize; i < reader.getBookmarks().size(); i++) {
				Bookmark data = reader.getBookmarks().get(i);
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				map = socialLinkWeightCalculator.getRankedTagListSocialLinkWeight(data.getUserID(), data.getTimestampAsLong(),
							true);
				results.add(map);
			}
			return results;
		}else {
			for (int i = trainSize; i < reader.getBookmarks().size(); i++) {
				Bookmark data = reader.getBookmarks().get(i);
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				if (algorithm.equals("social_freq")) {
					map = new SocialFrequencyCalculator(userTagTimes, network, users)
							.getRankedTagListSocialFrequency(data.getUserID(), data.getTimestampAsLong(), true);
				} else if (algorithm.equals("social")) {
					map = new SocialBLLCalculator(userTagTimes, network, users).getRankedTagListSocial(data.getUserID(), data.getTimestampAsLong(), exponentSocial);
				} else if (algorithm.equals("hybrid")) {
					map = new SocialHybridCalculator(userTagTimes, network, users, resultMapPersonalBLLAllUsers, resultMapPersonalFreqAllUsers)
							.getRankedTagListSocialBLLHybrid(data.getUserID(), data.getTimestampAsLong(), beta, exponentSocial, sort, false);
				} else if(algorithm.equals("hybrid_link")){
				    SocialHybridCalculator socialHybridCalculator =  new SocialHybridCalculator(userTagTimes, network, users, resultMapPersonalBLLAllUsers, resultMapPersonalFreqAllUsers);
				    socialHybridCalculator.setSocialStrengthCalculator(socialStrengthCalculator);
				    map = socialHybridCalculator.getRankedTagListSocialBLLHybrid(data.getUserID(), data.getTimestampAsLong(), beta, exponentSocial, sort, true);
				} else if (algorithm.equals("hybrid_freq")) {
					map = new SocialHybridCalculator(userTagTimes, network, users, resultMapPersonalBLLAllUsers, resultMapPersonalFreqAllUsers)
							.getRankedTagListSocialFrequencyHybrid(data.getUserID(), data.getTimestampAsLong(), beta);
				} else if (algorithm.equals("social_recency")) {
					map = new SocialRecencyRecommender(userTagTimes, network, users)
							.getRankedTagListSocialRecency(data.getUserID(), data.getTimestampAsLong(), true);
				}

				
				
				results.add(map);
			}
			return results;
		}
	}

	/**
	 * The entrance point for creating the result for all the test set.
	 * 
	 * @param betaBLL
	 *            weight for the BLL score
	 * @param betaCB
	 *            parameter for the content values
	 * @param dSocial
	 *            parameter for exponent for social recommendation.
	 * @param lambdaSocial
	 *            weight for the social score
	 * @param algorithm
	 *            The algorithm for which we want to get the recommendation.
	 * @param contentBasedValues
	 *            content based values.
	 * @return return the {@link BookmarkReader} object after computing the
	 *         recommendation for the test dataset.
	 */
	public BookmarkReader predictSample(double betaBLL, double betaCB, double dSocial, Double lambdaSocial,
			String algorithm, Map<Integer, Map<Integer, Double>> contentBasedValues, String suffix) {
		List<Map<Integer, Double>> resultValues = null;
		List<Map<Integer, Double>> actValues = null;
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		List<Bookmark> testLines = reader.getTestLines();
		if (contentBasedValues == null) {
			actValues = calculateSocialTagScore(betaBLL, dSocial, algorithm, true);
			resultValues = actValues;
		} else {
			actValues = calculateSocialTagScore(betaBLL, dSocial, algorithm, false);
			resultValues = new ArrayList<Map<Integer, Double>>();
			int i = 0;
			for (Map<Integer, Double> actMap : actValues) {
				int userID = testLines.get(i++).getUserID();
				Map<Integer, Double> contentMap = contentBasedValues.get(userID);
				if (contentMap == null) {
					resultValues.add(null);
					continue;
				}
				if (actMap != null && actMap.entrySet() != null) {
					for (Map.Entry<Integer, Double> actEntry : actMap.entrySet()) {
						if (actEntry != null && actEntry.getKey() != null) {
							actEntry.setValue(betaCB * actEntry.getValue());
						}
					}
				}

				if (contentMap != null && contentMap.entrySet() != null) {
					for (Map.Entry<Integer, Double> contentEntry : contentMap.entrySet()) {
						if (contentEntry != null && contentEntry.getKey() != null) {
							Double actVal = actMap.get(contentEntry.getKey());
							double contentVal = (1.0 - betaCB) * contentEntry.getValue();
							actMap.put(contentEntry.getKey(),
									actVal == null ? contentVal : actVal.doubleValue() + contentVal);
						}
					}
				}

				// Map<Integer, Double>sortedActMap = new TreeMap<Integer,
				// Double>(new DoubleMapComparator(actMap));
				// sortedActMap.putAll(actMap);
				// resultValues.add(sortedActMap);
				resultValues.add(MapUtil.sortByValue(actMap));
				// resultValues.add(MergeUtil.mergeMapsWithThreshold(contentMap,
				// actMap, 10));
			}
		}

		List<int[]> predictionValues = new ArrayList<int[]>();
		if (resultValues != null) {
			for (int i = 0; i < resultValues.size(); i++) {
				Map<Integer, Double> resultMap = resultValues.get(i);
				if (resultMap != null && resultMap.keySet() != null) {
					predictionValues.add(Ints.toArray(resultMap.keySet()));
				}else{
				    predictionValues.add(null);
				}
			}
		}

		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(this.filename + "_" + suffix);
		return reader;
	}

    public double getEta_h() {
        return eta_h;
    }

    public void setEta_h(double eta_h) {
        this.eta_h = eta_h;
    }

    public double getEta_l() {
        return eta_l;
    }

    public void setEta_l(double eta_l) {
        this.eta_l = eta_l;
    }
    
    public SocialStrengthCalculator getSocialStrengthCalculator() {
        return socialStrengthCalculator;
    }

    public void setSocialStrengthCalculator(SocialStrengthCalculator socialStrengthCalculator) {
        this.socialStrengthCalculator = socialStrengthCalculator;
    }
}
