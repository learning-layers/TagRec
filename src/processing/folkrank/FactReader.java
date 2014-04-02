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


/** A FactReader returns every single fact as an array of strings (for each 
 * dimension one string). By reading the facts until hasNext() == false one 
 * can gather all facts of a dataset. 
 * 
 * @author rja
 */
public interface FactReader {

    /** Returns the next fact as an array of strings - one string for each 
     * dimension.
     * 
     * @return An Array of strings, one string for each dimension.
     * @throws FactReadingException
     */
    public String[] getFact() throws FactReadingException;
    /** Tests if there are more facts available to read. 
     *
     * @return <code>true</code> if there are more facts to read.
     * @throws FactReadingException
     */
    public boolean hasNext() throws FactReadingException;
    /** Resets the fact reader so that the facts can be read again. 
     * <strong>Note:</strong> some implementations may not return the same facts 
     * after calling reset() since the facts may have changed since the last 
     * reading.  
     *  
     * @throws FactReadingException
     */
    public void reset() throws FactReadingException;
    /** Returns the number of dimensions of the fact source.
     * @return An integer determining the number of dimensions. 
     * @throws FactReadingException
     */
    public int getNoOfDimensions() throws FactReadingException;
    /** Close the reader. One a reader has been closed, reset(), hasNext() or 
     * getFact() operations will throw a FactReadingException.
     * 
     * @throws FactReadingException
     */
    public void close () throws FactReadingException;
    
}
