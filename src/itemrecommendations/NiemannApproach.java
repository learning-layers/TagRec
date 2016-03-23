package itemrecommendations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.vecmath.GVector;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import processing.BM25Calculator;
import common.Bookmark;
import common.DoubleMapComparator;
import common.Features;
import common.Resource;
import common.Session;
import common.Similarity;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

public class NiemannApproach {


	private final int DAY = 0;
	BookmarkReader reader; 
	List<Integer> user;
	private int numberOfTopics;
	private List<Bookmark> trainList;
	//double lambda;
	//BM25Calculator rankedResourseCalculator;

	//listId = userId, Set= resourceIds
	//private List<Set<Integer>> userResourceTrainList;


	// listId = resourceId; mapKey = topicId; mapValue = count
	private List<Map<Integer, Integer>> resTopicTrainList;

	private String sampleName;
	private int trainSize;
	private List<Integer> uniqueUserList;
	private HashMap<Integer, List<Integer>> resourceListPerUser;
	private List<Session> sessionList;
	private HashMap<Integer, Resource> sessionResourceMap;
	private int maxCluster;
	private int minCluster;
	private int maxResources;
	private int minResources;
	
	

	public NiemannApproach(String sampleName, int trainSize){

		this.trainSize = trainSize;
		this.sampleName = sampleName;
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(sampleName);
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		//	this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + testSize);

		//rankedResourseCalculator = new BM25Calculator(this.reader, this.trainSize, false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
		this.numberOfTopics = this.reader.getCategories().size();
		this.resourceListPerUser = new HashMap<Integer, List<Integer>>();

		this.resTopicTrainList = Utilities.getResTopics(this.trainList);

		//this.resTopicTestList = Utilities.getResTopics(this.testList);
		
		this.uniqueUserList = reader.getUniqueUserListFromTestSet(trainSize);
		this.sessionList = new LinkedList<Session>();
		this.sessionResourceMap = new HashMap<Integer, Resource>();
	}

	public BookmarkReader predictResources(int sessionType, String timezone, int similaritySetSize, int sampleSize) {
		this.generateSessions(sessionType, timezone);
		this.countObjectOccurences();
		this.calculateMI();
		this.calculateSimilarity(similaritySetSize);
		
		//TODO: calculate similarity, prediction (am ähnlichsten zur letzten userSession?)
		LinkedList<int[]> sortedResourcesPerUser = new LinkedList<int[]>();
		for (Integer userId : this.uniqueUserList) {
			sortedResourcesPerUser.add(predict(userId, sampleSize));
		}
		
		PredictionFileWriter writer = new PredictionFileWriter(reader, sortedResourcesPerUser);
		String outputFile = this.sampleName;
		writer.writeResourcePredictionsToFile(outputFile + "_sustain", this.trainSize, 0);
		return this.reader;
	}

	private int[] predict(Integer userId, int sampleSize) {
		
		TreeMap<Integer, Double> sortedResourceSimilarityMap = new TreeMap<Integer, Double>();
		
		for (String resource : this.reader.getResources()){
			if (Bookmark.getResourcesFromUser(this.trainList, userId).contains(resource))
				continue;
		}	
		
		//TODO: continue here!
		//sortedResourceActivationMap.putAll(resourceActivationMap);
		int[] sortedResources = new int[sampleSize];
		/*
		int index =0;
		for (int resourceId : sortedResourceActivationMap.navigableKeySet()){
			sortedResources[index] = resourceId;
			index ++;
			if (index == sampleSize)
				break;
		}*/
		return sortedResources; 
	}

	private void calculateSimilarity(int consideredResources) {
		for (Resource r1 : this.sessionResourceMap.values()){
			for (Resource r2 : this.sessionResourceMap.values()){
				if (r1.equals(r2))
					continue;
				r1.addSim(r2.id, Utilities.getCosineFloatSim(r1.getHighestMIs(consideredResources),r2.getHighestMIs(consideredResources)));				
			} 
		}
	}

	private void calculateMI() {
		for (Entry<Integer, Resource> r1 : this.sessionResourceMap.entrySet()){
			for (Entry<Integer, Resource> r2 : this.sessionResourceMap.entrySet()){
				if (r1.equals(r2))
					continue;
				r1.getValue().calculateMI(r2.getValue(), this.sessionList.size());				
			} 
		}
	}

	private void countObjectOccurences() {
		for (Session session : this.sessionList){
			for (int r1 : session.getResources()){
				Resource r = this.sessionResourceMap.getOrDefault(r1, new Resource(r1));
				r.increment();
				for (int r2 : session.getResources()){
					if (r1==r2)
						continue;
					r.incrementCoocurrence(r2);
				}
			}
		}
		
	}

	private void generateSessions(int sessionType, String timezone) {
		List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(this.trainList, false);
		
		for (List<Bookmark> userList : userBookmarks){
			long lastActivity = 0;
			Session session = new Session();
			for (Bookmark bm : userList){
								
				if (lastActivity != 0 && !this.inSession(lastActivity, Long.valueOf(bm.getTimestamp()), "Europe/Vienna", sessionType)){ 
					this.sessionList.add(session);
					session = new Session();
				}
				session.addBookmark(bm);
				lastActivity= Long.valueOf(bm.getTimestamp());
			}
			this.sessionList.add(session);
		}    
	}

	private boolean inSession(long lastActivity, long newActivity, String timezone, int sessionType){
		if (sessionType == DAY){
			if (lastActivity-newActivity>=1)
				System.err.println("userBookmarks not sorted!!");
			DateTime t1 = new DateTime(lastActivity,DateTimeZone.forTimeZone(TimeZone.getTimeZone(timezone)));
			DateTime t2 = new DateTime(newActivity,DateTimeZone.forTimeZone(TimeZone.getTimeZone(timezone)));
			return t1.withTimeAtStartOfDay().isEqual(t2.withTimeAtStartOfDay());
		}
		
		Duration d = new Duration(lastActivity, newActivity);
		return (d.getStandardMinutes()<=sessionType);
	}
}
