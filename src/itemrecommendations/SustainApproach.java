package itemrecommendations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	//double lambda;
	BM25Calculator rankedResourseCalculator;
	
	//listId = userId, Set= resourceIds
	//private List<Set<Integer>> userResourceTrainList;
	
	
	// listId = resourceId; mapKey = topicId; mapValue = count
	private List<Map<Integer, Integer>> resTopicTrainList;

	private Map<Integer, ArrayList<GVector>> userClusterList;
	private Map<Integer, Double> userLearningRateList;
	private Map<Integer, GVector> userLambdaList;
	private String sampleName;
	private int trainSize;
	private List<Integer> uniqueUserList;
	private Map<Integer, List<Integer>> resourceListPerUser;
	
	private int maxCluster;
	private int minCluster;
	private int maxResources;
	private int minResources;
	
	public SustainApproach(String sampleName, int trainSize){
		
		this.maxCluster = 0;
		this.maxResources = 0;
		this.minResources = trainSize;
		this.minCluster = trainSize;
		
		this.trainSize = trainSize;
		this.sampleName = sampleName;
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(sampleName);
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
	//	this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + testSize);
	
		rankedResourseCalculator = new BM25Calculator(this.reader, this.trainSize, false, true, false, 5, Similarity.COSINE, Features.ENTITIES);
		this.userLearningRateList = new HashMap<Integer, Double>();
		this.resourceListPerUser = new HashMap<Integer, List<Integer>>();
		//go through all users - matrix user-resource
		// Set is ordered per user? TODO: ask Dominik, Set can not be ordered linkedHashSet can. Is there a method to get sorted resources? 
		//this.userResourceTrainList = Utilities.getUserResourceLists(this.trainList);
		
		
		//this.userResourceTestList =  Utilities.getUserResourceLists(this.testList);
	    //go through all unique resources - Erstelle resourcen topic matrix
		
	
	
		
		this.uniqueUserList = reader.getUniqueUserListFromTestSet(trainSize);
		
	
		//saves Cluster per user
		this.userClusterList = new HashMap<Integer, ArrayList<GVector>>();
		//saves lambda per user
		this.userLambdaList = new HashMap<Integer, GVector>();
		
		
		//TODO: check, is this necessary
		//this.reader.setUserLines(reader.getBookmarks().subList(trainSize, trainSize + testSize));
	}
	
	
	
	public int getMaxCluster() {
		return maxCluster;
	}

	public int getMinCluster() {
		return minCluster;
	}

	public int getMaxResources() {
		return maxResources;
	}

	public int getMinResources() {
		return minResources;
	}



	public BookmarkReader predictResources(double r, double tau, double beta, double learningRate, double gamma, int trainingRecency, int candidateNumber, int sampleSize, double cfWeight, boolean onTags) {
		
		if (onTags){
			this.resTopicTrainList = Utilities.getResMaps(this.trainList);
			this.numberOfTopics = this.reader.getTags().size();
		}
		else{	
			this.resTopicTrainList = Utilities.getResTopics(this.trainList);
			this.numberOfTopics = this.reader.getCategories().size();
		}
		
		//
		
		// for every user
		for (Integer userId : this.uniqueUserList) {
			//TODO: pass the last 5 items
			List<Integer> resourceList = Bookmark.getResourcesFromUser(this.trainList, userId);
		    if (resourceList.size()>=trainingRecency && trainingRecency!=0)
		    	resourceList = resourceList.subList(resourceList.size()-trainingRecency, resourceList.size()); 
			
		    this.resourceListPerUser.put(userId, resourceList);
		    train(userId, resourceList, r, tau, learningRate, beta, gamma);
		}
		
		
		this.writeUserLambdas(this.sampleName);
		this.writeUserStats(this.sampleName);
		
		
		LinkedList<int[]> sortedResourcesPerUser = new LinkedList<int[]>();
		for (Integer userId : this.uniqueUserList) {
//			if (userId%100 ==0)
//				System.out.println("user "+userId+" of "+this.uniqueUserList.size());
			//FIXME: change back learningRate
			//sortedResourcesPerUser.add(predict(userId,  r, tau, learningRate, beta, candidateNumber, sampleSize, cfWeight));
			sortedResourcesPerUser.add(predict(userId,  r, tau, this.userLearningRateList.get(userId), beta, candidateNumber, sampleSize, cfWeight));
		}
		
		PredictionFileWriter writer = new PredictionFileWriter(reader, sortedResourcesPerUser);
		String outputFile = this.sampleName;
		writer.writeResourcePredictionsToFile(outputFile + "_sustain", this.trainSize, 0);
	 
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
					String resultString = reader.getUsers().get(entry.getKey()) + "| ";
					
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

	private boolean writeUserStats(String filename) {
		
		//List<String> resourceList = this.reader.getResources();
		//Map<Integer, List<Integer>> resourcesOfTestUsers = this.reader.getResourcesOfTestUsers(trainSize);
		
		try {
			FileWriter writer = new FileWriter(new File("./data/metrics/" + filename + "_userstats.txt"));
			BufferedWriter bw = new BufferedWriter(writer);
			
//			String header = "userId|number of cluster|number of resources\n";
//			bw.write(header);

			for (Entry<Integer, GVector> entry : this.userLambdaList.entrySet()) {
				//String resultString = (this.reader.getUsers().get(userID) + "-XYZ|");
				int userId = entry.getKey();
				
				
				String resultString = reader.getUsers().get(userId) + ";";
				
				double lambdas = 0.0;
								
				for (int c=0; c<entry.getValue().getSize(); c++) {
					lambdas += entry.getValue().getElement(c);
				}
				
				double entropy = 0.0;
				
				for (int c=0; c<entry.getValue().getSize(); c++) {
					double fraction = entry.getValue().getElement(c)/lambdas;
					entropy += fraction* Math.log(1/fraction);
				}
				
				//Entropy, number of Cluster, number of Resources
				resultString += String.valueOf(entropy)+";"+this.userClusterList.get(userId).size()+
						";"+this.resourceListPerUser.get(userId).size()+";"+this.userLearningRateList.get(userId)+"\n";
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
	
	
	private void train(int userId, List<Integer> list, double r, double tau, double learningRate, double beta, double gamma){
		ArrayList<GVector> clusterList = new ArrayList<GVector>();
		
		double[] array = new double[this.numberOfTopics];
		Arrays.fill(array,1);
		GVector lambda = new GVector(array);
	
		
		double maxUserActivation = 0.0;
		double minUserActivation = 1.0;
	    int resourceCount =0;
		
		//Start training with each resource in the list
		for (Integer resource : list){
			resourceCount++;
			learningRate = learningRate*Math.pow(resourceCount,-gamma);
			Set<Integer> topics = this.resTopicTrainList.get(resource).keySet();
				
			// Vector, write 1 for every existing topic ->Paul 1000
			GVector currentResource = new GVector(this.numberOfTopics);
			currentResource.zero();
			for (Integer t : topics){
				currentResource.setElement(t, 1);
			}	
			
			if (clusterList.size()==0){
				clusterList.add(currentResource);
				continue;
			}
			
			
			
			double maxActivation = 0;
			GVector bestCluster = new GVector(0);
			
			// create new vector with all extracted topics f.i. 500  
			GVector minDistance = new GVector(this.numberOfTopics);
			minDistance.zero();
		    Double totalActivation = 0.0;
			int index = 0;
			int bestIndex=0;
			
			for (GVector c : clusterList){
				//fixme_changed
				Pair<Double, GVector> activationPair = this.calculateActivation(currentResource, c, lambda, r, topics);
				
				// activation Pair -> left=activation, right = distanceVector
				if (activationPair.getLeft()>maxActivation){
					bestCluster = c;							
					minDistance= new GVector(activationPair.getRight());
					maxActivation= activationPair.getLeft();
					bestIndex = index;
				}
				totalActivation += activationPair.getLeft();
				index++;
			}
			
			
			//maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation;
			
			if (maxActivation<=tau){
				// input forms a new cluster
				bestCluster = currentResource;
				clusterList.add(bestCluster);
				//clusterList.add(index, bestCluster);
				bestIndex = index;
			}
			
			//System.out.println("max activation before Hemmung: "+maxActivation);
			// equation 6 Hemmung
		// 	maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation; 
			//System.out.println("max activation nach Hemmung: "+maxActivation);
		
			
			GVector deltaLambda = new GVector(lambda.getSize());
			deltaLambda.zero();
			// equation 13
			for (int i =0; i<lambda.getSize(); i++){
				//if (lambda.getElement(i)==0)
					//continue;
				double elementProduct = lambda.getElement(i)*minDistance.getElement(i);
				deltaLambda.setElement(i, learningRate*Math.exp(-elementProduct)*(1-elementProduct));
			}
			//GVector.add = adds the two vectors elements
			lambda.add(deltaLambda); 
		 
			//maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation;
			// equation 12
			
			// FIXME: error!
			GVector deltaBestCluster = new GVector(bestCluster.getSize());
			//  delta_winCluster <- n*(I-Cluster[WinCluster,]) # eq 12 
			deltaBestCluster.sub(currentResource, bestCluster);
		    deltaBestCluster.scale(learningRate);
		  		  
		    bestCluster.add(deltaBestCluster);
		    clusterList.set(bestIndex, bestCluster);
		    //clusterList.add(bestCluster);
		    
		    if (maxUserActivation<maxActivation)
		    	maxUserActivation = maxActivation;
		    if (minUserActivation>maxActivation)
		    	minUserActivation = maxActivation;
		}
		//if (clusterList.size()>1)
	//	System.out.println(clusterList.size()+"cluster for user "+userId+" with "+list.size()+" resources and activation "+minUserActivation+" to "+maxUserActivation);
		int numCluster = clusterList.size();
		if (numCluster<minCluster)
			minCluster = numCluster;
		if (numCluster>maxCluster)
			maxCluster = numCluster;
		
		int numResources = list.size();
		if (numResources<this.minResources)
			this.minResources=numResources;
		if (numResources>this.maxResources)
			this.maxResources=numResources;
		
		this.userLambdaList.put(userId, lambda);
		this.userClusterList.put(userId, clusterList);
		this.userLearningRateList.put(userId, learningRate);
	}
	
	
	private Pair<Double, GVector> calculateActivation(GVector input, GVector cluster, GVector lambda, double r, Set<Integer> topics){
		// Calculate distance for every cluster # eq 4	 
		GVector distance = new GVector(input.getSize());
		distance.sub(input, cluster);
		
		// * 0.5 is removed, since we do not map 2 values for each topic, but only one 
		for (int i =0; i<distance.getSize(); i++){
			
			distance.setElement(i, Math.abs(distance.getElement(i)));
			// distance is set to 1 for all topics that not used by the current resource
			if (input.getElement(i)==0){
				distance.setElement(i, 1);
			}	
		}
		
		double numerator=0;
		double denom=0;
		
		// Calculate cluster activation # eq 5	 
		for (int i =0; i<lambda.getSize(); i++){
			if (topics.contains(i)){
				double lambdaR = Math.pow(lambda.getElement(i), r);
				denom = denom+lambdaR;
				numerator= numerator+lambdaR*Math.exp((-lambda.getElement(i)*distance.getElement(i)));
			}
				
		}
		
		return new ImmutablePair<Double, GVector>((numerator/denom), distance);
	}
	
	
	private int[] predict(int userId, double r, double tau, double learningRate, double beta, int candidateNumber, int sampleSize, double alpha){
		Map<Integer, Double> resourceActivationMap = new HashMap<Integer, Double>();

		
//		if (candidateNumber>0){
			Map<Integer, Double> candidateSet = this.rankedResourseCalculator.getRankedResourcesList(userId, true, false, false);
			//TreeMap<Integer, Double> candidateSet = this.calculateCandidateSet(userId);
			Map<Integer, Double> CFValues = new HashMap<Integer, Double>();
			
			int count = 0;
			
			for (Map.Entry<Integer, Double> resource : candidateSet.entrySet()){
				if (candidateNumber>0 && count == candidateNumber)
					break;
				double resourceActivation = this.calculateResourceActivations(userId, resource.getKey(), beta, r);
				resourceActivationMap.put(resource.getKey(), resourceActivation);
				CFValues.put(resource.getKey(), resource.getValue());
				count++;
			}
			
			this.calculateNormalizedValues(resourceActivationMap);
			this.calculateNormalizedValues(CFValues);
			
			for ( Entry<Integer, Double> entry : resourceActivationMap.entrySet()){
				double valueSustain = entry.getValue();
				double valueCF = CFValues.get(entry.getKey());
				double activation = entry.getValue()*alpha+CFValues.get(entry.getKey())* (1-alpha);	
				resourceActivationMap.put(entry.getKey(), activation);
	//		}
					
		}
//		else{
//			TreeMap<Integer, Double> candidateSet = (TreeMap<Integer, Double>) this.rankedResourseCalculator.getRankedResourcesList(userId, true, false, false);
//		//for (int resource =0; resource< this.resTopicTrainList.size(); resource++){
//			for (Map.Entry<Integer, Double> resource : candidateSet.entrySet()){
//				if (Bookmark.getResourcesFromUser(this.trainList, userId).contains(resource))
//					continue;
//				double resourceActivation = this.calculateResourceActivations(userId, resource.getKey(), beta, r);
//				
//				double activation = resourceActivation*(alpha);
//				activation+=resource.getValue()*(1-alpha);
//				
//				resourceActivationMap.put(resource.getKey(), activation);
//			}
//		
//		}	
			
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
	
	
//	
//	private Map<Integer, Double> calculateNormalizedValues(Map<Integer, Double> values) {
//		//normalize
//		double sum =0;
//		for (Map.Entry<Integer, Double> entry : values.entrySet()) {
//			 sum += entry.getValue();
//		}
//		 for (Map.Entry<Integer, Double> entry : values.entrySet()) {
//			entry.setValue(1000/sum *entry.getValue());
//		}
//		return values;
//	}

	private void calculateNormalizedValues(Map<Integer, Double> values) {
		//normalize
		double sum =0;
		for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			 sum += entry.getValue();
		}
		 for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			entry.setValue(entry.getValue()/sum);
		}
		 sum=0;
		 for (Map.Entry<Integer, Double> entry : values.entrySet()) {
			 sum += entry.getValue();
		}
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
			Pair<Double, GVector> activationPair = this.calculateActivation(currentResource, c, this.userLambdaList.get(userId), r, topics);
			if (activationPair.getLeft()>maxActivation){
				maxActivation = activationPair.getLeft();
			}
			totalActivation+= activationPair.getLeft();
		}
		
		//TODO: Think about whether this should be included or not
		//maxActivation = Math.pow(maxActivation, beta)/Math.pow(totalActivation, beta)*maxActivation;
		return maxActivation;
		//return totalActivation;
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


