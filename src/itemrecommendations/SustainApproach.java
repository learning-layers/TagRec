package itemrecommendations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.Bookmark;
import common.DoubleMapComparator;
import common.Utilities;
import file.BookmarkReader;
import file.PredictionFileWriter;

import javax.vecmath.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


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
	private List<Bookmark> testList;	
	double lambda;
	
	//listId = userId, Set= resourceIds
	private List<Set<Integer>> userResourceTrainList;
	// listId = resourceId; mapKey = topicId; mapValue = count
	private List<Map<Integer, Integer>> resTopicTrainList;

	private List<Set<Integer>> userResourceTestList;
	private List<Map<Integer, Integer>> resTopicTestList;
	private LinkedList<ArrayList<GVector>> userClusterList;
	private LinkedList<GVector> userLambdaList;
	private String sampleName;
	private int trainSize;
	
	public SustainApproach(String sampleName, int trainSize, int sampleSize){
		
		this.trainSize = trainSize;
		this.sampleName = sampleName;
		this.reader = new BookmarkReader(trainSize, false);
		this.reader.readFile(sampleName);
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
	//	this.testList = this.reader.getBookmarks().subList(trainSize, trainSize + testSize);
	
		this.numberOfTopics = this.reader.getCategories().size();
		//go through all users - matrix user-resource
		// Set is ordered per user?
		this.userResourceTrainList = Utilities.getUserResourceLists(this.trainList);
	
		//this.userResourceTestList =  Utilities.getUserResourceLists(this.testList);
	    //go through all unique resources - Erstelle resourcen topic matrix
		
		//TODO: ask Dominik
		this.resTopicTrainList = Utilities.getResTopics(this.trainList);
		
		//this.resTopicTestList = Utilities.getResTopics(this.testList);
		
		//saves Cluster per user
		this.userClusterList = new LinkedList<ArrayList<GVector>>();
		//saves lambda per user
		this.userLambdaList = new LinkedList<GVector>();
		
		//TODO: check, is this necessary
		//this.reader.setUserLines(reader.getBookmarks().subList(trainSize, trainSize + testSize));
	}
	
	
	public BookmarkReader predictResources(double r, double tau, double beta, double learningRate) {
			// for every user
		for (Set<Integer> resourceSet : this.userResourceTrainList){
			train(resourceSet, r, tau, learningRate, beta);
		}
		
		int id = 0;
		LinkedList<int[]> sortedResourcesPerUser = new LinkedList<int[]>();
		int sampleSize = 20;

		for (Set<Integer> resourceSet : this.userResourceTrainList){
			if (id%100 ==0)
				System.out.println("user "+id+" of "+this.userResourceTrainList.size());
			sortedResourcesPerUser.add(predict(resourceSet, id,  r, tau, learningRate, beta, sampleSize));
			id ++;
		}
		
		PredictionFileWriter writer = new PredictionFileWriter(reader, sortedResourcesPerUser);
		String outputFile = this.sampleName;
		writer.writeResourcePredictionsToFile(outputFile + "_sustain", this.trainSize, 0);
	 
	   return this.reader;
	}

	
	private void train(Set<Integer> resourceSet, double r, double tau, double learningRate, double beta){
		//LinkedList<Integer> topics = new LinkedList<Integer>();
		ArrayList<GVector> clusterList = new ArrayList<GVector>();
				
		double[] array = new double[this.numberOfTopics];
		Arrays.fill(array,1);
		GVector lambda = new GVector(array);
		//clusterList.add(c0);
		
		for (Integer resource : resourceSet){
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
		this.userLambdaList.add(lambda);
		this.userClusterList.add(clusterList);
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
	
	
	private int[] predict(Set<Integer> resourceSet, int userId, double r, double tau, double learningRate, double beta, int sampleSize){
		Map<Integer, Double> resourceActivationMap = new HashMap<Integer, Double>();
		for (int resource = 0; resource < this.resTopicTrainList.size(); resource++){
			
			// If a resource is in the training set of a user do not predict it
			if (resourceSet.contains(resource))
				continue;
			
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
			resourceActivationMap.put(resource, maxActivation);
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
}
