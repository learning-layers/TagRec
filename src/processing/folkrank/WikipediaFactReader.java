/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package processing.folkrank;

import common.Bookmark;
import file.BookmarkReader;

public class WikipediaFactReader implements FactReader {

	private BookmarkReader reader;
	private int trainSize;
	private int lineIndex;
	private int tagIndex;
    private int noOfDimensions;

    public WikipediaFactReader (BookmarkReader reader, int trainSize, int noOfDimensions) {
        this.noOfDimensions = noOfDimensions;
        this.reader = reader;
        this.trainSize = trainSize;
        this.lineIndex = 0;
        this.tagIndex = -1;
    }

    public String[] getFact() throws FactReadingException {
    	Bookmark data = this.reader.getBookmarks().get(this.lineIndex);
    	String[] fact = new String[this.noOfDimensions];

    	fact[0] = data.getTags().get(this.tagIndex).toString();
    	fact[1] = Integer.toString(data.getUserID());
    	fact[2] = Integer.toString(data.getWikiID());
    	
    	return fact;
    }

    public boolean hasNext() throws FactReadingException {
    	if (this.lineIndex < this.trainSize) {
    		Bookmark data = this.reader.getBookmarks().get(this.lineIndex);
    		if (++this.tagIndex < data.getTags().size()) {
    			return true;
    		} else {
    			this.tagIndex = 0;
    			if (++this.lineIndex < this.trainSize) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public void reset() throws FactReadingException {
    	this.lineIndex = 0;
    	this.tagIndex = -1;
    }

    public int getNoOfDimensions() throws FactReadingException {
        return this.noOfDimensions;
    }

    public void close() throws FactReadingException {
    	reset();
    }
}
