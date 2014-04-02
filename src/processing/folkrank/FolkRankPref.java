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

/** The <code>FolkRankPref</code> class holds the preference weights for the 
 * FolkRank algorithm. 
 *   
 * @author rja
 */
public class FolkRankPref {

    /** For each dimension a list of items which will get extra preference 
     * weight. 
     */
    private int[][] prefItems       = null;
    /** For each dimension a list of preference weights which will be given to 
     * the items in prefItems. 
     */
    private double[][] prefValues   = null;
    /** For each dimension the weight each item gets. 
     *
     */
    private double[] basePrefWeight = null;

    /** Sets for each dimension the preference weight <em>each node</em> gets.      
     * @param basePrefWeight - For each dimension the weight <em>each item</em>
     * gets.
     */
    public FolkRankPref (double[] basePrefWeight) {
        this.basePrefWeight = basePrefWeight;
    }
    
    /** Set the items which will get extra preference and the corresponding 
     * preferene values.
     * 
     * @param prefItems - For each dimension an array of items which will get
     * extra preference.
     * @param prefValues - For each dimension an array of preference values 
     * which the corresponding item from prefItems will get.
     */
    public void setPreference (int[][] prefItems, double[][] prefValues) {
        this.prefItems  = prefItems;
        this.prefValues = prefValues;
    }
    
    /** Sets for each dimension the preference weight <em>each node</em> gets.      
     * @param basePrefWeight - For each dimension the weight <em>each item</em>
     * gets.
     */
    /*private void setBasePrefWeight(double[] basePrefWeight) {
        this.basePrefWeight = basePrefWeight;
    }*/

    /** Returns the array which contains for each dimension the items which 
     * should get extra preference weight.
     * 
     * @return An array of items which should get extra preference weight.
     */
    public int[][] getPrefItems() {
        return prefItems;
    }
    /** Returns an array which contains for each dimension the values the items
     * in prefItems get as extra preference.
     * 
     * @return An array of preference values.
     */
    public double[][] getPrefValues() {
        return prefValues;
    }

    /** Returns the preference weight for each dimension which 
     * <em>each item</em> gets.
     * 
     * @return An array of preference weights.
     */
    public double[] getBasePrefWeight() {
        return basePrefWeight;
    }
}
