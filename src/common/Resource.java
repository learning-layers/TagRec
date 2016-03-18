package common;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Resource {
	int id; 
	int occurrence;
	HashMap<Integer, Integer> cooccurrence;
    Map<Double, Integer> MIs;  
	
	public Resource(int id){
		this.id = id;
		this.occurrence = 0;
		this.cooccurrence = new HashMap<Integer, Integer>();
		this.MIs = new TreeMap<Double, Integer>();
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
		// Math.log is base e, natural log, ln
		return Math.log( x ) / Math.log( 2 );
    }
	
	
	public void getHighestMIs(int number){
		//this.MIs.descendingKeySet();???
	}
	
}
