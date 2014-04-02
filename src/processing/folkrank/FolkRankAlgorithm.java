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

public class FolkRankAlgorithm {

    private FolkRankResult baselineResult = null;
    private FolkRankParam  param = null;
    private FolkRankPref   pref = null;
    
    public FolkRankAlgorithm (FolkRankParam param) {
        this.param = param;
    }
    
    public void resetBaseline () {
        baselineResult = null;
        param = null;
    }
    
    public FolkRankResult computeFolkRank (final FolkRankData facts, final FolkRankPref pref) {
        /*
         * initalize baseline, if neccessary
         */
        if (baselineResult == null) {
            baselineResult = compute(facts, new FolkRankPref(pref.getBasePrefWeight()));
        }
        /*
         * compute weights with preference
         */
        FolkRankResult preferenceResult = compute(facts, pref);
        /*
         * compute difference weights
         */
        double[][] baselineWeights = baselineResult.getWeights();
        double[][] preferenceWeights = preferenceResult.getWeights();
        /*
         * for evaluation: remember weights of adapted PageRank
         */
        preferenceResult.setAPRWeights(preferenceWeights);  

        for (int dim = 0; dim < baselineWeights.length; dim++) {
            for (int item = 0; item < baselineWeights[dim].length; item++) {
                preferenceWeights[dim][item] -= baselineWeights[dim][item];
            }
        }
        return preferenceResult;
    }
    
    public FolkRankResult compute (final FolkRankData factsData, final FolkRankPref pref) {

        /*
         * These vectors are used to control the final weight computation:
         * alpha * oldWeight  +  beta * newWeight  +  gamma * pref
         * with alpha + beta + gamma = 1 
         */
        double alpha = param.getAlpha();
        double beta  = param.getBeta();
        double gamma = param.getGamma();

        /*
         * input data
         */
        int[][] facts  = factsData.getFacts();
        int[][] counts = factsData.getCounts();

        /*
         * output data
         */
        //FolkRankResult result = new StandardFolkRankResult();
        FolkRankResult result = new APRFolkRankResult(); // TODO: choose correct one
        
        
        /*
         * weight vectors
         */
        double[][] weights    = new double[counts.length][]; // weight 
        double[][] newWeights = new double[counts.length][]; // temporary weight
        double[][] spread     = new double[counts.length][]; // spreading weight

        double[] pWeights     = new double[counts.length]; // random surfer weights
        double[] pWeightsSum  = new double[counts.length]; // sum of random surfer + preference weights

        int[][] prefNodes     = pref.getPrefItems();  // nodes with preference
        double[][] prefValues = pref.getPrefValues(); // preference values 
        

        /*
         * allocate memory for weight vectors
         */
        for (int dim = 0; dim < weights.length; dim++) {
            newWeights[dim] = new double[counts[dim].length];
            spread[dim]     = new double[counts[dim].length];
            weights[dim]    = new double[counts[dim].length];
        }
        
        /*
         * initialize weights, random surfer, preference sums
         */
        
        param.getWeightInitializationStrategy().initalizeWeights(pref, weights, pWeights, pWeightsSum);
        
        
        
        
        /* ********************************************************************
         * main loop
         * ********************************************************************/
        int iter = 0;
        double delta = Double.MAX_VALUE;
        while (iter < param.getMaxIter() && delta > param.getEpsilon()) {
            iter++;

            /*
             * initalize new weights with zero
             */
            for (double[] newWeightsDim:newWeights) {
                Arrays.fill(newWeightsDim, 0.0);
            }

            /*
             * initalize spread
             */
            for (int dim = 0; dim < spread.length; dim++) {
                for (int node = 0; node < spread[dim].length; node++) {
                    spread[dim][node] = weights[dim][node] / counts[dim][node];  
                }
            }

            /*
             * spread weight
             */
            for (int fact[]: facts) {
                for (int dim = 0; dim < newWeights.length; dim++) {
                    /*
                     * newWeights[dim][fact[dim]] gets weight from all other
                     * nodes at this hyperedge
                     */
                    for (int dimAdd = 0; dimAdd < newWeights.length; dimAdd++) {
                        if (dim != dimAdd) {
                            /*
                             * get weight from surrounding nodes
                             */
                            newWeights[dim][fact[dim]] += spread[dimAdd][fact[dimAdd]];
                        } else {
                            /*
                             * ignore own weight
                             */
                        }
                    }
                }
            }

            /*
             * calculate new weights
             */
            delta = 0.0;
            double newWeight;
            for (int dim = 0; dim < weights.length; dim++) {
                
                /*
                 * spread preference
                 */
                if (prefValues != null && prefNodes != null) {
                    for (int node = 0; node < prefNodes[dim].length; node++) {
                        /*
                         * TODO: explain
                         */
                        double prefSpread = (gamma / beta) * (prefValues[dim][node] / pWeightsSum[dim]); 
                        newWeights[dim][prefNodes[dim][node]] += prefSpread;
                    }
                }                
                
                /*
                 * accumulate old weight, new weight and random surfer into
                 * new weight
                 */
                double sumOld  = 0;
                double sumNew  = 0;
                double sumPref = 0;
                for (int node = 0; node < weights[dim].length; node++) {
                    
                    double oldW = alpha * weights[dim][node];
                    double newW = beta  * newWeights[dim][node];
                    double prefW = gamma * pWeights[dim];
                    
                    sumOld  += oldW;
                    sumNew  += newW;
                    sumPref += prefW;
                    
                    newWeight = oldW + 
                                newW + 
                                prefW; 
                    
                    /*
                     * compute weight change
                     */
                    delta += Math.abs(weights[dim][node] - newWeight);
                    
                    weights[dim][node] = newWeight;
                }
            }
            
            result.addError(delta);
            
        } // main loop
        
        result.setWeights(weights);
        
        return result;
    }

}
