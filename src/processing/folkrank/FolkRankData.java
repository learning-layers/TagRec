/**
 * Copyright (c) 2006-2009, NEPOMUK Consortium
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 * 	documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the NEPOMUK Consortium nor the names of its 
 *       contributors may be used to endorse or promote products derived from 
 * 	this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 **/
package processing.folkrank;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** This class holds the data the FolkRank computation is based on. In 
 * particular, it contains a list of all facts, a mapping of internal ids to 
 * strings and counts of the item occurences in the facts. 
 * 
 * @author rja
 */
public class FolkRankData {

    private int[][] facts;
    private int[][] counts;
    private Date date;
    private String[][] keyToValueMapping;
    private Map<String, Integer>[] valueToKeyMapping;
    
    /** Constructs a new fact array. The size of numberOfItemsPerDimensions
     * defines how many dimensions will be used.
     * 
     * @param noOfFacts - the number of facts.
     * @param noOfItemsPerDimension - the number of items for each dimension. 
     */
    public FolkRankData (int noOfFacts, int[] noOfItemsPerDimension) {
        facts   = new int[noOfFacts][];
        keyToValueMapping = new String[noOfItemsPerDimension.length][];
        valueToKeyMapping = new HashMap[noOfItemsPerDimension.length];
        counts  = new int[noOfItemsPerDimension.length][];
        this.date = new Date(); // TODO: it might be useful to set this from outside 
        
        for (int dim = 0; dim < counts.length; dim++) {
            /*
             * initialize counts with zero 
             */
            counts[dim] = new int[noOfItemsPerDimension[dim]];
            Arrays.fill(counts[dim], 0);
            /*
             * initialize mappings to null
             * (mappings are initialized in addMapping(), when needed.
             */
            keyToValueMapping[dim] = null;
        }
    }
    
    /** Adds for this dimension the mapping of key to value to the list of 
     * mappings. 
     * 
     * @param dimension - the dimension the mapping belongs to.
     * @param key - the key of the mapping.
     * @param value - the value of the mapping.
     */
    public void addMapping (int dimension, int key, String value) {
        /*
         * initialize mapping for this dimension, if not initialized, yet.
         */
        if (keyToValueMapping[dimension] == null) {
            keyToValueMapping[dimension] = new String[counts[dimension].length];
        }
        keyToValueMapping[dimension][key] = value;
    }
    
    /** Stores the inverse mapping.
     * @param dimension
     * @param mapping 
     */
    public void addInverseMapping (final int dimension, final Map<String, Integer> mapping) {
        valueToKeyMapping[dimension] = mapping;
    }
    
    /** Returns the mapping (integer) for value <code>key</code>.
     * @param dimension
     * @param key
     * @return
     */
    public int getInverseMapping (final int dimension, final String key) {
        return valueToKeyMapping[dimension].get(key);
    }
    
    /** Checks, if there exists an inverse mapping for the given key.
     * 
     * @param dimension
     * @param key
     * @return
     */
    public boolean hasInverseMapping (final int dimension, final String key) {
        return valueToKeyMapping[dimension].containsKey(key);
    }
    
    /** Returns the collected mappings. 
     * 
     * @return - an array of strings which contains in every array of
     * its first dimension the mapping for the corresponding dimension.
     */
    public String[][] getMapping() {
        return keyToValueMapping;
    }

    /** Sets the fact with the specified id.
     * 
     * @param factId - position in fact array.
     * @param fact - value to be written into fact array.
     */
    public void setFact (int factId, int[] fact) {
        facts[factId] = fact;
        /*
         * Count occurences of items in this fact. Each item is counted twice,
         * since one hyperedge is interpreted as 2*noOfDimensions directed 
         * edges.
         */
        for (int dim = 0; dim < fact.length; dim++) {
            counts[dim][fact[dim]] += 2;
        }
    }
    
    /** Returns the fact array.
     * 
     * @return An array of facts.
     */
    public int[][] getFacts() {
        return facts;
    }

    /** Returns the counts for each item in each dimension.
     * 
     * @return An array of counts.
     */
    public int[][] getCounts() {
        return counts;
    }
    
    
    /** Returns the date of the current dataset.
     * @return The date of the dataset. 
     */
    public Date getDate() {
        return date;
    }
    
}
