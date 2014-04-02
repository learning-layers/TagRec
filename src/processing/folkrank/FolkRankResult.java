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

import java.util.LinkedList;

public interface FolkRankResult {

    /** Returns the weight vectors for each dimension.
     * 
     * @return An array of weight vectors - one vector for each dimension.
     */
    public double[][] getWeights();

    /** Sets the weight vectors.
     * 
     * @param weights - An array of weight vectors - one for each dimension.
     */
    public void setWeights(double[][] weights);

    /** Returns a list of errors from the computation. For each iteration one
     * error value.
     *  
     * @return A list of double values. For each iteration one value. 
     */
    public LinkedList<Double> getErrors();

    /** Add an error value to the list of error values.
     * 
     * @param error - An error value.
     */
    public void addError(double error);

    
    /** Returns the weight vectors of the adapted PageRank for each dimension.
     * 
     * @return An array of weight vectors - one vector for each dimension.
     */
    public double[][] getAPRWeights();

    /** Sets the weight vectors for the adapted PageRank.
     * 
     * @param weights - An array of weight vectors - one for each dimension.
     */
    public void setAPRWeights(double[][] weights);
    
}