package common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class Resource {
	public int id; 
	int occurrence;
	HashMap<Integer, Integer> cooccurrence;
	HashMap<Integer, Double> similarResources;
    TreeMap<Double, Integer> MIs; 
    double log2;
	
	public Resource(int id){
		this.id = id;
		this.occurrence = 0;
		this.cooccurrence = new HashMap<Integer, Integer>();
		this.MIs = new TreeMap<Double, Integer>();
		this.log2 = Math.log( 2 );
		this.similarResources = new HashMap<Integer, Double>();
	}
	
	public void increment(){
		this.occurrence++;
	} 
	
	public void incrementCoocurrence(int resource){
		this.cooccurrence.put(resource, this.cooccurrence.getOrDefault(resource,0)+1);
	}


	public Integer getCooccurrence(int resource) {
		return cooccurrence.get(resource);
	}
	
	
	// n = number of all resources
	public void calculateMI (Resource r1, int n){
		int n11 = this.cooccurrence.get(r1.id);
		int n00 = n-this.occurrence-r1.occurrence-n11;
		int n0_ = r1.occurrence * n00;
		int n_0 = this.occurrence * n00;
		int n1_ = this.occurrence * n11;
		int n_1 = r1.occurrence * n11;
		
		double mi = n11/n * this.log2(n*n11/n1_*n_1)
				+r1.occurrence/n  * this.log2(n*r1.occurrence/n0_*n_1)
				+this.occurrence/n  * this.log2(n*this.occurrence/n_0*n1_)
				+n00/n  * this.log2(n*n00/n0_*n_0);
		
		this.MIs.put(mi, r1.id);
	}
	
	private double log2( double x )
    {
		return Math.log( x ) / this.log2;
    }
	
	
	public HashMap<Integer, Double> getHighestMIs(int number){
		//this.MIs.descendingKeySet();???
		HashMap<Integer, Double> highestMIs = new HashMap<Integer, Double>();
		int count = 0;
		for (Map.Entry<Double, Integer> entry :this.MIs.descendingMap().entrySet()){
			highestMIs.put(entry.getValue(), entry.getKey());
			if (++count == number)
				break;
		}
		
		return highestMIs;
	}

	public void addSim(int resourceId, double similarity) {
		this.similarResources.put(resourceId, similarity);		
	}
	
}
