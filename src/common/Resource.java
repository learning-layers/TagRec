package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.joda.time.LocalDateTime;

public class Resource {
	public int id; 
	int occurrence;
	HashMap<Integer, Integer> cooccurrence;
	public HashMap<Integer, Double> similarResources;
    TreeMap<Double, ArrayList<Integer>> MIs; 
    double log2;
    HashSet<Integer> tags;
	
	public Resource(int id){
		this.id = id;
		this.occurrence = 0;
		this.cooccurrence = new HashMap<Integer, Integer>();
		this.MIs = new TreeMap<Double, ArrayList<Integer>>();
		this.log2 = Math.log( 2 );
		this.similarResources = new HashMap<Integer, Double>();
		this.tags = new HashSet<Integer>();
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
//		System.out.println(r1.id);
		double n11 = this.cooccurrence.getOrDefault(r1.id, 0);
		double n10 = this.occurrence-n11;
		double n01 = r1.occurrence-n11;			
		
		double n00 = n-n11-n10-n01;
		double n0_ = n01 + n00;
		double n_0 = n10 + n00;
		double n1_ = this.occurrence;
		double n_1 = r1.occurrence;
		
		double mi; 
		
		
			double m1 = (n11/n) * this.log2((n*n11)/(n1_*n_1));
			if (Double.isNaN(m1)) m1=0;
			double m2	= (n10/(double)n)  * this.log2((n*n10)/(n1_*n_0));
			if (Double.isNaN(m2)) m2=0;
			double m3	= (n01/(double)n)  * this.log2((n*n01)/(n0_*n_1));
			if (Double.isNaN(m3)) m3=0;
			double m4	= (n00/n)  * this.log2((n*n00)/(n0_*n_0));
			if (Double.isNaN(m4)) m4=0;
			mi=m1+m2+m3+m4;
		

		ArrayList<Integer> list = this.MIs.get(mi);
		if (list == null){
			list = new ArrayList<Integer>();
			this.MIs.put(mi, list);
		}	
		
		list.add(r1.id);
	}
	
	private double log2( double x )
    {
		return Math.log( x ) / this.log2;
		//return Math.log( x ) / this.log2;
		
    }
	
	
	public HashMap<Integer, Double> getHighestMIs(int number){
		HashMap<Integer, Double> highestMIs = new HashMap<Integer, Double>();
		int count = 0;
		for (Entry<Double, ArrayList<Integer>> entry :this.MIs.descendingMap().entrySet()){
			for (Integer resource : entry.getValue()){
				highestMIs.put(resource, entry.getKey());
				if (++count == number)
					return highestMIs;
			}	
		}
		
    	
		return highestMIs;
	}

	public void addSim(int resourceId, double similarity) {
		if (similarity >0)
			this.similarResources.put(resourceId, similarity);		
	}
	
	public void addTags(List<Integer> tags){
		this.tags.addAll(tags);
	}
	
	public HashSet<Integer> getTags(){
		return this.tags;
	}
}
