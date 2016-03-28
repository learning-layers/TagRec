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
import org.joda.time.LocalDateTime;
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
	private List<Bookmark> trainList;
	//double lambda;
	//BM25Calculator rankedResourseCalculator;

	//listId = userId, Set= resourceIds
	//private List<Set<Integer>> userResourceTrainList;


	// listId = resourceId; mapKey = topicId; mapValue = count

	private String sampleName;
	private int trainSize;
	private List<Integer> uniqueUserList;
	private List<Session> sessionList;
	private HashMap<Integer, Resource> sessionResourceMap;
	private HashMap<Integer, Integer> resourceTagMap;
	
	//last Session of a user
	private HashMap<Integer, Session> userSessionMap;

	

	public NiemannApproach(String sampleName, int trainSize){

		this.trainSize = trainSize;
		this.sampleName = sampleName;
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(sampleName);
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		//	this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + testSize);

		//rankedResourseCalculator = new BM25Calculator(this.reader, this.trainSize, false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
//		this.numberOfTopics = this.reader.getCategories().size();
//		this.resourceListPerUser = new HashMap<Integer, List<Integer>>();
//
//		this.resTopicTrainList = Utilities.getResTopics(this.trainList);

		//this.resTopicTestList = Utilities.getResTopics(this.testList);
		
		this.uniqueUserList = reader.getUniqueUserListFromTestSet(trainSize);
		this.sessionList = new LinkedList<Session>();
		this.sessionResourceMap = new HashMap<Integer, Resource>();
		this.userSessionMap = new HashMap<Integer, Session>();
	}

	public BookmarkReader predictResources(int sessionDuration, String timezone, int similaritySetSize, int sampleSize, boolean predictTags) {
		this.generateSessions(sessionDuration, timezone);
		System.out.println(new LocalDateTime().toString()+"Sessions generated");
		this.countObjectOccurences();
		System.out.println(new LocalDateTime().toString()+"coocurences counted");
		this.calculateMI();
		System.out.println(new LocalDateTime().toString()+"Mis");
		this.calculateSimilarity(similaritySetSize);
		System.out.println(new LocalDateTime().toString()+"similarities");
		
		//TODO: calculate similarity, prediction (am ähnlichsten zur letzten userSession?)
		LinkedList<int[]> sortedResourcesPerUser = new LinkedList<int[]>();
	
		int countUser =0;
		for (Integer userId : this.uniqueUserList) {
			sortedResourcesPerUser.add(predict(userId, sampleSize, predictTags));
			countUser++;
			if (countUser%100 == 0)
				System.out.println(new LocalDateTime().toString()+" "+countUser+" of "+this.uniqueUserList.size());
		}
		System.out.println(new LocalDateTime().toString()+"prediction finished");
		PredictionFileWriter writer = new PredictionFileWriter(reader, sortedResourcesPerUser);
		String outputFile = this.sampleName;
		writer.writeResourcePredictionsToFile(outputFile + "_niemann", this.trainSize, 0);
		return this.reader;
	}

	private int[] predict(Integer userId, int sampleSize, boolean predictTags) {
		
		
		HashMap<Integer, Double> itemSimMap = new HashMap<Integer, Double>();
		//List<Integer> userResources = Bookmark.getResourcesFromUser(this.trainList, userId);
		List<Integer> userResources = new ArrayList<Integer> (this.userSessionMap.get(userId).getResources());
		
		if (predictTags){
			HashMap<Integer, Integer> tagOccurenceCount = new HashMap<Integer, Integer>();
//			for (int tag: this.sessionResourceMap.get(simRes.getKey()).getTags()){
//				itemSimMap.put(tag, itemSimMap.getOrDefault(tag, 0.0)+simRes.getValue());
//				tagOccurenceCount.put(tag, tagOccurenceCount.getOrDefault(tag, 0)+1);
//			}
			for (Map.Entry<Integer,Double> tagValue : itemSimMap.entrySet())
				itemSimMap.put(tagValue.getKey(), tagValue.getValue()/tagOccurenceCount.get(tagValue.getKey()));
								
		}
		else{
			 
			for (int resId : userResources){
				Resource userRes = this.sessionResourceMap.get(resId);
				for (Map.Entry<Integer, Double> simRes : userRes.similarResources.entrySet()){
					if (userResources.contains(simRes.getKey()))
						continue;
					itemSimMap.put(simRes.getKey(), itemSimMap.getOrDefault(simRes.getKey(), 0.0)+simRes.getValue());
				}	
			}

			
		}
			
		TreeMap<Integer, Double> sortedItemSimMap = new TreeMap<Integer, Double>(new DoubleMapComparator(itemSimMap));
		sortedItemSimMap.putAll(itemSimMap);
		int[] sortedResources = new int[sampleSize];
		
		int index =0;
		for (int resourceId : sortedItemSimMap.navigableKeySet()){
			sortedResources[index] = resourceId;
			index ++;
			if (index == sampleSize)
				break;
		}
		
		return sortedResources; 
	}

	private void calculateSimilarity(int consideredResources) {
		for (Resource r1 : this.sessionResourceMap.values()){
			for (Resource r2 : this.sessionResourceMap.values()){
				if (r1.id == r2.id)
					continue;
				r1.addSim(r2.id, Utilities.getCosineFloatSim(r1.getHighestMIs(consideredResources),r2.getHighestMIs(consideredResources)));
				 
			} 
		}
	}

	private void calculateMI() {
		for (Entry<Integer, Resource> r1 : this.sessionResourceMap.entrySet()){
			for (Entry<Integer, Resource> r2 : this.sessionResourceMap.entrySet()){
				if (r1.getValue() == r2.getValue())
					continue;
				r1.getValue().calculateMI(r2.getValue(), this.sessionList.size());		
			} 
		}
	}

	private void countObjectOccurences() {
		for (Session session : this.sessionList){
			for (int r1 : session.getResources()){
				Resource r = this.sessionResourceMap.get(r1);
				r.increment();
				for (int r2 : session.getResources()){
					if (r1==r2)
						continue;
					r.incrementCoocurrence(r2);
				}
			//	this.sessionResourceMap.put(r.id, r);
			}
		}
		
	}

	private void generateSessions(int sessionDuration, String timezone) {
		List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(this.trainList, false);
		
		for (List<Bookmark> userList : userBookmarks){
			
			long lastActivity = 0;
			Session session = new Session();
			for (Bookmark bm : userList){
				if (lastActivity != 0 && !this.inSession(lastActivity, Long.valueOf(bm.getTimestamp()), "Europe/Vienna", sessionDuration)){ 
					this.sessionList.add(session);
					session = new Session();
				}
				session.addBookmark(bm);
				Resource r = this.sessionResourceMap.getOrDefault(bm.getWikiID(), new Resource(bm.getWikiID()));
				r.addTags(bm.getTags());
				this.sessionResourceMap.put(r.id, r);
				
				lastActivity= Long.valueOf(bm.getTimestamp());
			}
			this.sessionList.add(session);
			this.userSessionMap.put(session.userID, session);
		}    
	}

	private boolean inSession(long lastActivity, long newActivity, String timezone, int sessionDuration){
		if (sessionDuration == DAY){
			if (lastActivity-newActivity>=1)
				System.err.println("userBookmarks not sorted!!");
			DateTime t1 = new DateTime(lastActivity,DateTimeZone.forTimeZone(TimeZone.getTimeZone(timezone)));
			DateTime t2 = new DateTime(newActivity,DateTimeZone.forTimeZone(TimeZone.getTimeZone(timezone)));
			return t1.withTimeAtStartOfDay().isEqual(t2.withTimeAtStartOfDay());
		}
		
		Duration d = new Duration(lastActivity, newActivity);
		return (d.getStandardMinutes()<=sessionDuration);
	}
}
