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

/** This strategy spreads over every dimension one unit of weight. This means 
 * that the (initial) weights sum up to one for every dimension (as do the 
 * prefWeights, when the preference is included).
 *    
 * @author rja
 */
public class SeparatedWeightInitializationStrategy implements WeightInitializationStrategy {
    
    /**
     * @see org.semanticdesktop.nepomuk.comp.folkpeer.folkrank.strategy.WeightInitializationStrategy#initalizeWeights(org.semanticdesktop.nepomuk.comp.folkpeer.folkrank.data.FolkRankParam, double[][], double[], double[])
     */
    public void initalizeWeights(FolkRankPref pref, double[][] weights, double[] prefWeights, double[] prefWeightsNormFactors) {
        /*
         * check input arguments
         */
        if (weights.length != prefWeights.length || weights.length != prefWeightsNormFactors.length) 
            throw new IllegalArgumentException("Sizes of first dimension of input parameters do not match.");
        
        double[] basePrefWeight = pref.getBasePrefWeight();
        double[][] prefValues   = pref.getPrefValues();

        for (int dim = 0; dim < weights.length; dim++) {
            /*
             * Initialize weights:
             * every item gets the reciprocal of the number of items in the 
             * dimensions it belongs to.
             */
            Arrays.fill(weights[dim], 1.0 / weights[dim].length);

            /*
             * Calculate the sum of the preference weights.
             */
            prefWeightsNormFactors[dim] = 0.0;
            if (prefValues != null) {
                for (double prefValue:prefValues[dim]) {
                    prefWeightsNormFactors[dim] += prefValue;
                }
            }
            
            /*
             * Calculate, how many additional weight is spread by the preference
             */
            prefWeightsNormFactors[dim] = basePrefWeight[dim] * weights[dim].length + prefWeightsNormFactors[dim];

            /*
             * initialize the preference weights each item gets
             */
            if (basePrefWeight[dim] == 0.0) {
                /*
                 * prevent 0.0 / 0.0 = NaN
                 */
                prefWeights[dim] = 0.0;
            } else {
                prefWeights[dim] = basePrefWeight[dim] / prefWeightsNormFactors[dim];
            }
        }
    }

}
