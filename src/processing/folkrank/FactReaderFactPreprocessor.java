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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** The FactReaderPreprocessor reads the facts from the FactReader and
 * returns a FolkRankData object. For more information on how this is done
 * have a look at the {@link #process()} method.  
 * 
 * @author rja
 */
public class FactReaderFactPreprocessor implements FactPreprocessor {

    private FolkRankData facts = null;
    private FactReader reader;
    private String[][] stringPrefItems = null;
    private int[][] intPrefItems = null;
    private boolean storeInverseMapping = false;

    /** Initialize the preprocessor with a reader.
     *  
     * @param reader - the reader which supplies the preprocessor with facts.
     */
    public FactReaderFactPreprocessor (final FactReader reader) {
        this.reader = reader;
    }

    public FactReaderFactPreprocessor (final FactReader reader, boolean storeInverseMapping) {
        this.reader = reader;
        this.storeInverseMapping = storeInverseMapping;
    }

    
    /** Process the data the reader returns. This is done by doing the following 
     * steps:
     * <ul>
     * <li>initalize a map to map strings to integers for every mode</li>
     * <li>initalize a list to store the facts as integers</li>
     * <li>iterate over the fact list from the reader and 
     *   <ul>
     *   <li>add a mapping (string to integer) to the map, if neccessary</li>
     *   <li>add the fact to integer fact list</li>
     *   </ul>
     * </li>
     * <li>copy integer fact list into an array</li>
     * <li>generate inverse mapping (integer to tring)</li>
     * </ul>
     * 
     * @see org.semanticdesktop.nepomuk.comp.folkpeer.folkrank.process.FactPreprocessor#process()
     */
    public void process() {
        try {
            int noOfDimensions = reader.getNoOfDimensions();
            
            /*
             * Initialize the map for the string->integer mapping.
             * This map stores for each dimension, which integer an item gets 
             * assigned.
             */
            @SuppressWarnings("unchecked")
            final Map<String, Integer> [] mapping = new HashMap[noOfDimensions];
            for (int dim = 0; dim < mapping.length; dim++) {
                mapping[dim] = new HashMap<String, Integer>();
            }
            
            /*
             * Initialize integer fact list. This list is used to temporarily 
             * store the fact list in memory before copying it into an array.
             * 
             * The reason for doing this is, that the number of facts is unknown
             * and it may not be possible to read the same facts twice by using
             * reset() from the fact reader. 
             * 
             */
            List<int[]> factList = new LinkedList<int []>();

            /*
             * Initialize mapping counters (save current integer for each 
             * dimension).
             */
            int noOfItemsPerDimension[] = new int[noOfDimensions];
            Arrays.fill(noOfItemsPerDimension, 0);

            /*
             * generate mapping and integer fact list
             */
            while (reader.hasNext()) {
                /*
                 * for every fact
                 */
                final String[] stringFact = reader.getFact();
                int[] intFact = new int[noOfDimensions];
                
                for (int dim = 0; dim < stringFact.length; dim++) {
                    /*
                     * get item for this dimension
                     */
                    final String item = stringFact[dim];
                    /*
                     * map new item
                     */
                    if (!mapping[dim].containsKey(item)) {
                        mapping[dim].put(item, noOfItemsPerDimension[dim]);
                        noOfItemsPerDimension[dim]++;
                    }
                    /*
                     * save new item in fact
                     */
                    intFact[dim] = mapping[dim].get(item);
                }
                /*
                 * store fact
                 */
                factList.add(intFact);
            }
            reader.close();
            
            /*
             * copy fact list into array
             */            
            int noOfFacts = factList.size();
            facts = new FolkRankData (noOfFacts, noOfItemsPerDimension);
            for (int factId = 0; factId < noOfFacts; factId++) {
                facts.setFact(factId, factList.remove(0));
            }
            factList.clear();
            
            /*
             * map preference items strings to integers  
             */
            if (stringPrefItems != null) {
                intPrefItems = new int[stringPrefItems.length][];
                for (int dim = 0; dim < stringPrefItems.length; dim++) {
                    intPrefItems[dim] = new int[stringPrefItems[dim].length];
                    for (int item = 0; item < stringPrefItems[dim].length; item++) {
                        intPrefItems[dim][item] = mapping[dim].get(stringPrefItems[dim][item]);
                    }
                }
            }
            
            /*
             * invert mapping and put it into array 
             */
            for (int dim = 0; dim < mapping.length; dim++) {
                final Iterator<String> it = mapping[dim].keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    facts.addMapping(dim, mapping[dim].get(key), key);
                    // delete mapping, if neccessary
                    if (!storeInverseMapping) it.remove();
                }
                /*
                 * store mapping, if neccessary
                 */
                if (storeInverseMapping) {
                    facts.addInverseMapping(dim, mapping[dim]);
                } else {
                    mapping[dim].clear();
                }
            }
            
        } catch (FactReadingException e) {
            /*
             * TODO: implement proper exception handling
             */
            e.printStackTrace();
        }
    }
    
    /** Returns the filled data for the FolkRank.
     * @see org.semanticdesktop.nepomuk.comp.folkpeer.folkrank.process.FactPreprocessor#getFolkRankData()
     */
    public FolkRankData getFolkRankData() {
        return facts;
    }

    /** Sets the preference items in string representation such that a 
     * subsequent call to {@link #process()} will map them to integers.
     * @see org.semanticdesktop.nepomuk.comp.folkpeer.folkrank.process.FactPreprocessor#setPrefItems(java.lang.String[][])
     */
    public void setPrefItems(String[][] prefItems) {
        this.stringPrefItems = prefItems;
    }

    /** Returns the integer representation of the preference items.
     * @see org.semanticdesktop.nepomuk.comp.folkpeer.folkrank.process.FactPreprocessor#getPrefItems()
     */
    public int[][] getPrefItems() {
        return intPrefItems;
    }
}
