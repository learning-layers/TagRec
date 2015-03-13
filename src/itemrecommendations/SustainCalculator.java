package itemrecommendations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.TreeMap;

import common.Bookmark;
import common.DoubleMapComparator;
import common.Features;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Similarity;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

import javax.vecmath.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Stopwatch;

// TODO: integrate time and memory consumption test
public class SustainCalculator {
	//Define parameters: potentiell veraenderbar
	//r=2 #2
	//beta=1 # 2
	//n=0.7 # 0.6
	//tau_cluster=0.7 #0.9
		
	BookmarkReader reader; 
	List<Integer> user;
	private int numberOfTopics;
	private List<Bookmark> trainList;
	private double lambda;
	private CFResourceCalculator rankedResourseCalculator;
	
	private Stopwatch timer;
	private Timer timerThread;
	private MemoryThread memoryThread;
	private String timeString;
	
	//listId = userId, Set= resourceIds
	//private List<Set<Integer>> userResourceTrainList;
	
	
	// listId = resourceId; mapKey = topicId; mapValue = count
	private List<Map<Integer, Integer>> resTopicTrainList;

	private Map<Integer, ArrayList<GVector>> userClusterList;
	private Map<Integer, GVector> userLambdaList;
	private String sampleName;
	private int trainSize;
	private List<Integer> uniqueUserList;
	private Map<Integer, List<Integer>> resourceListPerUser;
	
	public SustainCalculator(String sampleName, int trainSize){
		this.timerThread = new Timer();
		this.memoryThread = new MemoryThread();
		this.timerThread.schedule(this.memoryThread, 0, MemoryThread.TIME_SPAN);
		
		this.trainSize = trainSize;
		this.sampleName = sampleName;
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(sampleName);
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
	//	this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + testSize);
	
		this.timer = new Stopwatch();
		this.timer.start();	
		rankedResourseCalculator = new CFResourceCalculator(this.reader, this.trainSize, false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
		
		this.numberOfTopics = this.reader.getCategories().size();
	
		//go through all users - matrix user-resource
		// Set is ordered per user? TODO: ask Dominik, Set can not be ordered linkedHashSet can. Is there a method to get sorted resources? 
		//this.userResourceTrainList = Utilities.getUserResourceLists(this.trainList);
		
		
		//this.userResourceTestList =  Utilities.getUserResourceLists(this.testList);
	    //go through all unique resources - Erstelle resourcen topic matrix
		
	
		this.resTopicTrainList = Utilities.getResTopics(this.trainList);
		
		//this.resTopicTestList = Utilities.getResTopics(this.testList);
		
		this.uniqueUserList = reader.getUniqueUserListFromTestSet(trainSize);
		
		
		//saves Cluster per user
		this.userClusterList = new HashMap<Integer, ArrayList<GVector>>();
		//saves lambda per user
		this.userLambdaList = new HashMap<Integer, GVector>();
		
		
		//TODO: check, is this necessary
		//this.reader.setUserLines(reader.getBookmarks().subList(trainSize, trainSize + testSize));
	}
	
	
	public BookmarkReader predictResources(double r, double tau, double beta, double learningRate, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight) {
		
		// for every user
		for (Integer userId : this.uniqueUserList) {
			//TODO: pass the last 5 items
			List<Integer> resourceList = Bookmark.getResourcesFromUser(this.trainList, userId);
		    if (resourceList.size()>=trainingRecency && trainingRecency!=0)
		    	resourceList = resourceList.subList(resourceList.size()-trainingRecency, resourceList.size()); 
			
		    train(userId, resourceList, r, tau, learningRate, beta);
		}
		
		
		this.writeUserLambdas(this.sampleName);
		timer.stop();
		long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timer.reset();
		timer.start();
		LinkedList<int[]> sortedResourcesPerUser = new LinkedList<int[]>();
		for (Integer userId : this.uniqueUserList) {
			if (userId%100 ==0)
				System.out.println("user "+userId+" of "+this.uniqueUserList.size());
			sortedResourcesPerUser.add(predict(userId,  r, tau, learningRate, beta, candidateNumber, sampleSize, cfWeight));
		}
		timer.stop();
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, sampleSize);
		
		PredictionFileWriter writer = new PredictionFileWriter(reader, sortedResourcesPerUser);
		String outputFile = this.sampleName;
		writer.writeResourcePredictionsToFile(outputFile + "_sustain", this.trainSize, 0);
	 
		this.timeString = PerformanceMeasurement.addMemoryMeasurement(this.timeString, false, this.memoryThread.getMaxMemory());
		this.timerThread.cancel();
		Utilities.writeStringToFile("./data/metrics/" + outputFile + "_sustain_TIME.txt", this.timeString);
	   return this.reader;
	}

	
	private boolean writeUserLambdas(String filename) {
		
			//List<String> resourceList = this.reader.getResources();
			//Map<Integer, List<Integer>> resourcesOfTestUsers = this.reader.getResourcesOfTestUsers(trainSize);
			
			try {
				FileWriter writer = new FileWriter(new File("./data/metrics/" + filename + "_lambdas.txt"));
				BufferedWriter bw = new BufferedWriter(writer);
				
		
				for (Entry<Integer, GVector> entry : this.userLambdaList.entrySet()) {
					//String resultString = (this.reader.getUsers().get(userID) + "-XYZ|");
					String resultString = entry.getKey() + "| ";
					
					String resultingLambdas = "";
									
					for (int c=0; c<entry.getValue().getSize(); c++) {
						resultingLambdas += entry.getValue().getElement(c)+", ";
					}
					
					if (resultingLambdas != "") {
						resultingLambdas = resultingLambdas.substring(0, resultingLambdas.length() - 2);
					}
					
					resultString += resultingLambdas+"\n";
					bw.write(resultString);
				
				}
				
				bw.flush();
				bw.close();
				writer.close();
							
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return false;
	}


	private void train(int userId, List<Integer> list, double r, double tau, double learningRate, double beta){
		//LinkedList<Integer> topics = new LinkedList<Integer>();
		ArrayList<GVector> clusterList = new ArrayList<GVector>();
				
		double[] array = new double[this.numberOfTopics];
		Arrays.fill(array,1);
		GVector lambda = new GVector(array);
		//clusterList.add(c0);
		//GVector bestCluster = new GVector(0);
		
		for (Integer resource : list){
			Set<Integer> topics = this.resTopicTrainList.get(resource).keySet();
				
			// Vector, write 1 for every existing topic
			GVector currentResource = new GVector(this.numberOfTopics);
			currentResource.zero();
			for (Integer t : topics)
				currentResource.setElement(t, 1);
			
			// create the first cluster
			if (clusterList.size()==0){
				clusterList.add(currentResource);				
				continue;
			}
			
			double maxActivation = 0;
			GVector bestCluster = new GVector(0);
			GVector minDistance = new GVector(this.numberOfTopics);
			minDistance.zero();
			Double totalActivation = 0.0;
			int index = 0;
			int bestIndex=0;
			for (GVector c : clusterList){
				Pair<Double, GVector> activationPair = this.calculateActivation(currentResource, c, lambda, r);
				if (activationPair.getLeft()>maxActivation){
					bestCluster = c;							
					minDistance= new GVector(activationPair.getRight());
					maxActivation= activationPair.getLeft();
					bestIndex = index;
				}
				totalActivation += activationPair.getLeft();
				index++;
			}
			
	//		System.out.println("test");
			// equation 6 Hemmung
			maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation; 
			
			if (maxActivation<=tau){
				// input forms a new cluster
				bestCluster = currentResource;
				//clusterList.add(bestCluster);
				clusterList.add(index, bestCluster);
				bestIndex = index;
			}
			
			GVector deltaLambda = new GVector(lambda.getSize());
			// equation 13
			for (int i =0; i<lambda.getSize(); i++){
				double elementProduct = lambda.getElement(i)*minDistance.getElement(i);
				deltaLambda.setElement(i, learningRate*Math.exp(-elementProduct)*(1-elementProduct));
			}
			//GVector.add = adds the two vectors elements
			lambda.add(deltaLambda); 
						
			// equation 12
			GVector deltaBestCluster = new GVector(bestCluster.getSize());
			//  delta_winCluster <- n*(I-Cluster[WinCluster,]) # eq 12 
			deltaBestCluster.sub(currentResource,deltaBestCluster);
		    deltaBestCluster.scale(learningRate);
		    
		    //??? why adding the cluster? 
		    bestCluster.add(deltaBestCluster);
		    clusterList.set(bestIndex, deltaBestCluster);
		}
		if (clusterList.size()>3)
			System.out.println(clusterList.size()+"cluster for user"+userId);
		
		this.userLambdaList.put(userId, lambda);
		this.userClusterList.put(userId, clusterList);
	}
	
	private Pair<Double, GVector> calculateActivation(GVector input, GVector cluster, GVector lambda, double r){
		// Calculate distance for every cluster # eq 4	 
		GVector distance = new GVector(input.getSize());
		distance.sub(input, cluster);
		
		for (int i =0; i<distance.getSize(); i++){
			// * 0.5 is removed, since we do not map 2 values for each topic, but only one 
			distance.setElement(i, Math.abs(distance.getElement(i)));
		}
		
		double numerator=0;
		double denom=0;
		// Calculate cluster activation # eq 5	 
		for (int i =0; i<lambda.getSize(); i++){
			double lambdaR = Math.pow(lambda.getElement(i), r);
			denom = denom+lambdaR;
			numerator= numerator+lambdaR*Math.exp((-lambda.getElement(i)*distance.getElement(i)));
		}
		
		return new ImmutablePair<Double, GVector>((numerator/denom), distance);
	}
	
	
	private int[] predict(int userId, double r, double tau, double learningRate, double beta, int candidateNumber, int sampleSize, double cfWeight){
		Map<Integer, Double> resourceActivationMap = new HashMap<Integer, Double>();
		
		if (candidateNumber>0){
			Map<Integer, Double> candidateSet = this.rankedResourseCalculator.getRankedResourcesList(userId, -1, true, false, false, true, false);
			//TreeMap<Integer, Double> candidateSet = this.calculateCandidateSet(userId);
			Map<Integer, Double> CFValues = new HashMap<Integer, Double>();
			
			int count = 0;
			for (Map.Entry<Integer, Double> resource : candidateSet.entrySet()){
				if (count == candidateNumber)
					break;
				double resourceActivation = this.calculateResourceActivations(userId, resource.getKey(), beta, r);
				resourceActivationMap.put(resource.getKey(), resourceActivation);
				CFValues.put(resource.getKey(), resource.getValue());
				count++;
			}
			
			//resourceActivationMap = this.calculateNormalizedValues(resourceActivationMap);
			//CFValues = this.calculateNormalizedValues(CFValues);
			
			for ( Entry<Integer, Double> entry : resourceActivationMap.entrySet()){
				double activation = entry.getValue()*(1-cfWeight)+CFValues.get(entry.getKey())* cfWeight;	
				resourceActivationMap.put(entry.getKey(), activation);
			}
					
		}
		else{
			Map<Integer, Double> candidateSet = this.rankedResourseCalculator.getRankedResourcesList(userId, -1, true, false, false, true, false);
			for (int resource =0; resource< this.resTopicTrainList.size(); resource++){
				if (Bookmark.getResourcesFromUser(this.trainList, userId).contains(resource))
					continue;
				double resourceActivation = this.calculateResourceActivations(userId, resource, beta, r);
				double activation = resourceActivation*(1-cfWeight);
					if (candidateSet.containsKey(resource))
						activation+=candidateSet.get(resource)*cfWeight;
				resourceActivationMap.put(resource, activation);
			}
		
		}	
			
		TreeMap<Integer, Double> sortedResourceActivationMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resourceActivationMap));
		sortedResourceActivationMap.putAll(resourceActivationMap);
		int[] sortedResources = new int[sampleSize];
		
		int index =0;
		for (int resourceId : sortedResourceActivationMap.navigableKeySet()){
			sortedResources[index] = resourceId;
			index ++;
			if (index == sampleSize)
				break;
		}
		return sortedResources; 
	}
	
	
	
	private Map<Integer, Double> calculateNormalizedValues(Map<Integer, Double> values) {
		//normalize
		double sum =0;
		for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			 sum += entry.getValue();
		}
		 for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			entry.setValue(1000/sum *entry.getValue());
		}
		return values;
	}

	private double calculateResourceActivations(int userId, int resource, double beta, double r){
	
		Set<Integer> topics = this.resTopicTrainList.get(resource).keySet();
		
		// Vector, write 1 for every existing topic
		GVector currentResource = new GVector(this.numberOfTopics);
		currentResource.zero();
		for (Integer t : topics)
			currentResource.setElement(t, 1);
		
		double maxActivation = 0.0;
		double totalActivation = 0.0;
		
		for (GVector c : this.userClusterList.get(userId)){
			Pair<Double, GVector> activationPair = this.calculateActivation(currentResource, c, this.userLambdaList.get(userId), r);
			if (activationPair.getLeft()>maxActivation){
				maxActivation = activationPair.getLeft();
			}
			totalActivation+= activationPair.getLeft();
		}
		
		maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation;
		return maxActivation;
	}
	
	private TreeMap<Integer, Double> calculateCandidateSet(int userId){
		GVector lastCluster = (this.userClusterList.get(userId)).get(this.userClusterList.get(userId).size()-1);
		HashMap<Integer, Double> topicMap = new HashMap<Integer, Double>();
		HashMap<Integer, Double> simMap = new HashMap<Integer, Double>();
		for (int c=0; c<lastCluster.getSize(); c++){
			if (lastCluster.getElement(c)>0)
				topicMap.put(c, lastCluster.getElement(c));
		}
			
		for (int resource =0; resource< this.resTopicTrainList.size(); resource++){
			if (Bookmark.getResourcesFromUser(this.trainList, userId).contains(resource))
				continue;
		   /// are the topics sorted
			Set<Integer> topics = this.resTopicTrainList.get(resource).keySet();
			HashMap<Integer, Double> resourceMap = new HashMap<Integer, Double>();
			
			for (Integer t : topics)
				resourceMap.put(t, 1.0);
			
			simMap.put(resource, Utilities.getCosineFloatSim(topicMap, resourceMap));
		}
		TreeMap<Integer, Double> sortedSimMap = new TreeMap<Integer, Double>(new DoubleMapComparator(simMap));
		sortedSimMap.putAll(simMap);
		return sortedSimMap;
	}
}


