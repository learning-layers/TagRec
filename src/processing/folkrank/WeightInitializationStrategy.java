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

/** A weight initialization strategy is used to initialize weight and preference 
 * vectors in a FolkRank computation. 
 * 
 * @author rja
 */
public interface WeightInitializationStrategy {

    /** Initializes the vectors weights, prefWeights and prefWeightsNormFactors 
     * with the help of the given parameters param. 
     * Note that the memory for all vectors must be allocated before calling 
     * initializeWeights!
     * 
     * @param pref - The preference to be used for initialization. 
     * 
     * @param weights - The output vector - "real" weight vector used for the 
     * FolkRank computation; initialized in this method.
     *  
     * @param prefWeights - The output vector which specifies for every 
     * dimension how many preference weight <em>each item</em> in that dimension 
     * gets.
     * 
     * @param prefWeightsNormFactors - The output vector which specifies for 
     * every dimension the value the additional preferences have to be divided 
     * with such that everything sums up to one.  
     */
    public abstract void initalizeWeights(
        FolkRankPref pref,
        double[][] weights,
        double[] prefWeights,
        double[] prefWeightsNormFactors);

}