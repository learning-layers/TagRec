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

/** This exception is thrown by FactReaders when an error occurs during 
 * initalization or reading.
 *  
 * @author rja
 */
public class FactReadingException extends Exception {

    /** Last time computed: 2007-02-01
     * 
     */
    private static final long serialVersionUID = -3668806000537549237L;

    /** Constructor which allows to add a message and an existing exception to
     * this exception.
     * 
     * @param message
     * @param cause
     */
    public FactReadingException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructor which allows to a add a message to this exception.
     * 
     * @param message
     */
    public FactReadingException(String message) {
        super(message);
    }

    /** Constructor which allows to add an existing exception to this exception.
     * @param cause
     */
    public FactReadingException(Throwable cause) {
        super(cause);
    }

}
