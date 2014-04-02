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

import java.util.SortedSet;
import java.util.TreeSet;

/** Internal class which is used to represent the top-k items together with
 * their weight in a set when finding them.
 * 
 * @author rja
 */
public class ItemWithWeight implements Comparable<ItemWithWeight> {
    /**
     * An int which represents the item.
     */
    public int item;
    /**
     * The weight of the item.
     */
    public double weight;
    /** The only available constructor. 
     *  
     * @param item - a string which represents the item.
     * @param weight - the weight of the item.
     */
    public ItemWithWeight(int item, double weight) {
        super();
        this.item = item;
        this.weight = weight;
    }
    /**
     * Disabled default constructor. 
     */
    private ItemWithWeight () {
        /*
         * do nothing, since this constructor is not useable.
         */ 
    }

    /** Returns the item.
     * @return - the item.
     */
    public int getItem() {
        return item;
    }
    /** Returns the weight of the item.
     * @return - the weight of the item.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof ItemWithWeight)) {
            return false;
        }
        return equals((ItemWithWeight) obj);
    }

    /** Two items are equal if there string representations are equal. This 
     * is true, since only items of the same dimension should be compared.
     * 
     * @param other - the item to compare with this item.
     * @return - true if this.item == other.item.
     */
    private boolean equals (ItemWithWeight other) {
        return this.item == other.item;
    }

    /** Compares two items by their weight. 
     * @param o - the other item to compare with this item.
     * @return - 0 if they're equal, -1/+1 otherwise.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ItemWithWeight o) {
        if (o == null) { throw new NullPointerException(); }
        int sgn = (int) Math.signum(o.weight - this.weight);
        if (sgn != 0) {
            return sgn;
        } else {
            return o.item - this.item;
        }
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode () {
        return item;
    }
    
    public static SortedSet<ItemWithWeight> getTopK (FolkRankData facts, double[][] weights, int k, int dim) {
        double minWeight;
        SortedSet<ItemWithWeight> set = new TreeSet<ItemWithWeight>();

        minWeight = -100; // consider only items with positive weight 
        for (int item = 0; item < weights[dim].length; item++) {
            double currWeight = weights[dim][item];
            if (currWeight > minWeight) {
                /* new weight to consider found */
                set.add(new ItemWithWeight(item, currWeight));
                if (set.size() > k) {
                    // new best weight, since we have more than k items in set
                    ItemWithWeight last = set.last();
                    set.remove(last);
                    minWeight = set.last().weight;
                }
            }
        }

        return set;
    }
}