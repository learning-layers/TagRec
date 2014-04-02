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

/** The <code>FolkRankParam</code> class holds the parameters of the FolkRank
 * algorithm. It does not contain any data used for the computation.
 *   
 * @author rja
 */
public class FolkRankParam {

    /* These three parameters must sum up to one. They determine how much of the
     * old weight, the new weight (result from weight spreading) and the 
     * preference determines the new weight vector. 
     */
    private double alpha = 0.0; // old weight
    private double beta  = 0.7; // new weight
    private double gamma = 0.3; // preference
    
    
    /** Stopping criterion 1: if computation error is smaller than epsilon (or 
     * number of iterations larger than maxIter), then computation is stopped.
     */
    private double epsilon = 10e-6;
    /** Stopping criterion 2: if number of iterations is larger than maxIter (or
     * computation error smaller than epsilon), then computation is stopped.
     */
    private int maxIter = 10;
    
    /** Since there are several ways how preference and weight could be 
     * initialized, different strategies how to do this can be implemented and
     * given to the FolkRank. 
     */
    private WeightInitializationStrategy weightInitializationStrategy = new SeparatedWeightInitializationStrategy();
    
  
    /** Set the main parameters for the FolkRank computation. Alpha, beta and 
     * gamma determine how much of the old, new, and preference weight, resp.
     * is used to determine the new weight. 
     * 
     * If alpha + beta + gamma = 1 does not hold, the parameters are normalized
     * such that they add up to one. 
     *  
     * @param alpha - Factor to determine which fraction of the old weight flows
     * into the new weight. The default is 0.0.
     * @param beta - Factor to determine which fraction of the new weight from 
     * the weight spreading flows into the new weight. The default is 0.7.
     * @param gamma - Factor to determine which fraction of the preference 
     * weight flows into the new weight. The default is 0.3.
     */
    public void setAlphaBetaGamma (double alpha, double beta, double gamma) {
        double sum = alpha + beta + gamma;
        if (Math.abs(sum - 1.0) < 10e-6) {
            this.alpha = alpha;
            this.beta  = beta;
            this.gamma = gamma;
        } else {
            /*
             * parameters do not add up to 1 ... normalize them
             */
            this.alpha = alpha / sum;
            this.beta  = beta  / sum;
            this.gamma = gamma / sum;
        }
    }
    
    /** Set the stopping conditions for the FolkRank computation. If either the 
     * number of iterations exceeds maxIter or the error drops below epsilon, 
     * the computation stops.
     *  
     * @param epsilon - The maximal error the computation should have before 
     * stopping. The default is 10e-6.
     * @param maxIter - The maximum number of iterations . The default is 10.
     */
    public void setStopCondition (double epsilon, int maxIter) {
        this.epsilon = epsilon;
        this.maxIter = maxIter;
    }
    
    /** Returns alpha.
     * 
     * @return The parameter alpha.
     */
    public double getAlpha() {
        return alpha;
    }
    /** Returns beta.
     * 
     * @return The parameter beta.
     */
    public double getBeta() {
        return beta;
    }
    /** Returns gamma.
     * 
     * @return The parameter gamma.
     */
    public double getGamma() {
        return gamma;
    }

    
    /** Returns epsilon, the error bound for the computation.
     * 
     * @return The error bound for the computation.
     */
    public double getEpsilon() {
        return epsilon;
    }
    /** Returns the maximal number of iterations.
     * 
     * @return The maximal number of iterations.
     */
    public int getMaxIter() {
        return maxIter;
    }
    
    /** Returns the weight initialization strategy.
     * 
     * @return The weight initialization strategy.
     */
    public WeightInitializationStrategy getWeightInitializationStrategy() {
        return weightInitializationStrategy;
    }
    /** Set the weight initialization strategy.
     * @param weightStrategy - The weight initialization strategy.
     */
    public void setWeightInitializationStrategy(WeightInitializationStrategy weightStrategy) {
        this.weightInitializationStrategy = weightStrategy;
    }

}
