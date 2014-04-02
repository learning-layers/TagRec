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


public class LeavePostOutFolkRankDataDuplicator {

    public static final int U = 1;
    public static final int R = 2;

    
    /** Precondition: leaving out u and r does NOT cause u or r to be completely 
     * removed (i.e. there exists at least one TAS with u and one TAS with r)
     * @param in
     * @param user
     * @param resource
     * @return
     */
    public FolkRankData getDuplicate (FolkRankData in, int user, int resource) {
        /*
         * count number of tas for this user/post combination
         */
        int[][] facts = in.getFacts();
        int tasCtr = 0;
        for (int fact[]: facts) {
            if (fact[U] == user && fact[R] == resource) tasCtr++;
        }
        /*
         * calculate number of items per dimension
         */
        int[][] counts = in.getCounts();
        int[] noOfItemsPerDimension = new int[counts.length];
        for (int dim = 0; dim < counts.length; dim++) {
            noOfItemsPerDimension[dim] = counts[dim].length;
        }
        FolkRankData out = new FolkRankData(facts.length - tasCtr, noOfItemsPerDimension);

        /*
         * copy fact
         */
        int factId = 0;
        for (int fact[]: facts) {
            if (fact[U] == user && fact[R] == resource) continue;
            out.setFact(factId, fact);
            factId++;
        }

        return out;
    }

    public FolkRankData getDuplicate (FolkRankData in, int user) {
        /*
         * count number of tas for this user/post combination
         */
        int[][] facts = in.getFacts();
        int tasCtr = 0;
        for (int fact[]: facts) {
            if (fact[U] == user) tasCtr++;
        }
        /*
         * calculate number of items per dimension
         */
        int[][] counts = in.getCounts();
        int[] noOfItemsPerDimension = new int[counts.length];
        for (int dim = 0; dim < counts.length; dim++) {
            noOfItemsPerDimension[dim] = counts[dim].length;
        }
        FolkRankData out = new FolkRankData(facts.length - tasCtr, noOfItemsPerDimension);

        /*
         * copy fact
         */
        int factId = 0;
        for (int fact[]: facts) {
            if (fact[U] == user) continue;
            out.setFact(factId, fact);
            factId++;
        }

        return out;
    }
}
