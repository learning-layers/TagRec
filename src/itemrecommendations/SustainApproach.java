package itemrecommendations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.Bookmark;
import common.DoubleMapComparator;
import common.Features;
import common.Similarity;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

import javax.vecmath.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import processing.BM25Calculator;


public class SustainApproach {
	//Define parameters: potentiell veraenderbar
	//r=2 #2
	//beta=1 # 2
	//n=0.7 # 0.6
	//tau_cluster=0.7 #0.9
		
	BookmarkReader reader; 
	List<Integer> user;
	private int numberOfTopics;
	private List<Bookmark> trainList;
	double lambda;
	BM25Calculator rankedResourseCalculator;
	
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
	
	public SustainApproach(String sampleName, int trainSize){
		
		
		this.trainSize = trainSize;
		this.sampleName = sampleName;
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(sampleName);
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
	//	this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + testSize);
	
		rankedResourseCalculator = new BM25Calculator(this.reader, this.trainSize, false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
		
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
	
	
	public BookmarkReader predictResources(double r, double tau, double beta, double learningRate, int trainingRecency, int candidateNumber, int sampleSize) {
		
		// for every user
		for (Integer userId : this.uniqueUserList) {
			//TODO: pass the last 5 items
			List<Integer> resourceList = Bookmark.getResourcesFromUser(this.trainList, userId);
		    if (resourceList.size()>=trainingRecency && trainingRecency!=0)
		    	resourceList = resourceList.subList(resourceList.size()-trainingRecency, resourceList.size()); 
			
		    train(userId, resourceList, r, tau, learningRate, beta);
		}
		
		LinkedList<int[]> sortedResourcesPerUser = new LinkedList<int[]>();
		
		for (Integer userId : this.uniqueUserList) {
			if (userId%100 ==0)
				System.out.println("user "+userId+" of "+this.uniqueUserList.size());
			
			sortedResourcesPerUser.add(predict(userId,  r, tau, learningRate, beta, candidateNumber, sampleSize));
		}
		
		PredictionFileWriter writer = new PredictionFileWriter(reader, sortedResourcesPerUser);
		String outputFile = this.sampleName;
		writer.writeResourcePredictionsToFile(outputFile + "_sustain", this.trainSize, 0);
	 
	   return this.reader;
	}

	
	private void train(int userId, List<Integer> list, double r, double tau, double learningRate, double beta){
		//LinkedList<Integer> topics = new LinkedList<Integer>();
		ArrayList<GVector> clusterList = new ArrayList<GVector>();
				
		double[] array = new double[this.numberOfTopics];
		Arrays.fill(array,1);
		GVector lambda = new GVector(array);
		//clusterList.add(c0);
		
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
					
			for (GVector c : clusterList){
				Pair<Double, GVector> activationPair = this.calculateActivation(currentResource, c, lambda, r);
				if (activationPair.getLeft()>maxActivation){
					bestCluster = c;							
					minDistance= new GVector(activationPair.getRight());
					maxActivation= activationPair.getLeft();
				}
				totalActivation += activationPair.getLeft(); 
			}
			
			// equation 6 Hemmung
			maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation; 
			
			if (maxActivation<=tau){
				// input forms a new cluster
				bestCluster = currentResource;
				clusterList.add(bestCluster);
			}
			
			GVector deltaLambda = new GVector(lambda.getSize());
			// equation 13
			for (int i =0; i<lambda.getSize(); i++){
				double elementProduct = lambda.getElement(i)*minDistance.getElement(i);
				deltaLambda.setElement(i, learningRate*Math.exp(-elementProduct)*(1-elementProduct));
			}
			lambda.add(deltaLambda); 
			
			// equation 12
			GVector deltaBestCluster = new GVector(bestCluster.getSize());
			//  delta_winCluster <- n*(I-Cluster[WinCluster,]) # eq 12 
			deltaBestCluster.sub(currentResource,deltaBestCluster);
		    deltaBestCluster.scale(learningRate);
		    bestCluster.add(deltaBestCluster);
		}
		
		this.userLambdaList.put(userId, lambda);
		this.userClusterList.put(userId, clusterList);
	}
	
	private Pair<Double, GVector> calculateActivation(GVector input, GVector cluster, GVector lambda, double r){
		// Calculate distance for every cluster # eq 4	 
		GVector distance = new GVector(input.getSize());
		distance.sub(input, cluster);
		
		for (int i =0; i<distance.getSize(); i++){
			distance.setElement(i, Math.abs(distance.getElement(i))*0.5);
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
	
	
	private int[] predict(int userId, double r, double tau, double learningRate, double beta, int candidateNumber, int sampleSize){
		Map<Integer, Double> resourceActivationMap = new HashMap<Integer, Double>();
		
		if (candidateNumber>0){
			Map<Integer, Double> candidateSet = this.rankedResourseCalculator.getRankedResourcesList(userId, true, false, false);
			int count = 0;
			for (Integer resource : candidateSet.keySet()){
				if (count == candidateNumber)
					break;
				Pair<Integer, Double> resourceActivation = this.calculateResourceActivations(userId, resource, beta, r);
				resourceActivationMap.put(resourceActivation.getLeft(), resourceActivation.getRight());
				count++;
			}
		}
		else{
			for (int resource =0; resource< this.resTopicTrainList.size(); resource++){
				if (Bookmark.getResourcesFromUser(this.trainList, userId).contains(resource))
					continue;
				Pair<Integer, Double> resourceActivation = this.calculateResourceActivations(userId, resource, beta, r);
				resourceActivationMap.put(resourceActivation.getLeft(), resourceActivation.getRight());
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
	
	private Pair<Integer, Double> calculateResourceActivations(int userId, int resource, double beta, double r){
	
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
		return new ImmutablePair<Integer, Double>(resource, maxActivation);
	}
}


