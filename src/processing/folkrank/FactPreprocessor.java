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

/** A FactPreprocessor reads facts from a source (for example a FactReader) and
 * creates a FolkRankData object. 
 * 
 * Since during the FolkRank computation all data is represented in integers 
 * (and there exists only the mapping back to strings in memory) there must be 
 * an option to set preference for items given by their strings. Therefore the 
 * interface demands the method setPrefItems which accepts items as strings and 
 * the method getPrefItems which returns them in integer representation. 
 *  
 * @author rja
 */
public interface FactPreprocessor {

    /*
     * NOTE: the methods here are given in the order they should typically be
     * called. Although some preprocessors might allow different call order.
     */
    
    
    /** Gives the preprocessor items for each dimension which the preprocessor
     * shall map to their integer representation during processing. The 
     * resulting integers can be accessed with getPrefItems().
     * 
     * @param prefItems - an array of strings for each dimension. Each string
     * represents an item occuring in the facts and which should be mapped to
     * an integer in order to give it preference. 
     */
    public void setPrefItems (String [][] prefItems);

    /** Calling this method starts the preprocessing which includes reading the
     * facts, mapping them into an array of integers, saving the integer to 
     * string mappings and saving the integers for the prefItems.
     */
    public void process ();
    
    /** Returns the complete input data neccessary for the FolkRank computation.
     * 
     * @return A FolkRankData object which contains the data for the 
     * computation.
     */
    public FolkRankData getFolkRankData ();

    /** Returns an integer array where the numbers in each dimension represent
     * the items given with setPrefItems(). This array can be used to set the
     * preference items in {@link FolkRankParam.setPreference}. 
     * @return - for each dimension an array of integers representing the items
     * given as strings with setPrefItems.
     */
    public int[][] getPrefItems ();
}
