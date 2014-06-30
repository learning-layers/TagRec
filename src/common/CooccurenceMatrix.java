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

package common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.javaml.clustering.mcl.SparseMatrix;
import net.sf.javaml.clustering.mcl.SparseVector;

public class CooccurenceMatrix {

	private SparseMatrix coocurenceMatrix;
	private List<Integer> tagCounts;

	public CooccurenceMatrix(List<Bookmark> bookmarks, List<Integer> tagCounts) {
		System.out.println("Building matrix ...");
		this.coocurenceMatrix = new SparseMatrix();
		this.tagCounts = tagCounts;
		this.initMatrix(bookmarks);
		normalizeMatrix();
		//calculateRelatedness();
	}

	private void initMatrix(List<Bookmark> bookmarks) {
		for (Bookmark bookmark : bookmarks) {
			List<Integer> tags = bookmark.getTags();
			/*for (int x = 0; x < tags.size() - 1; x++) {
				int tagIdx = tags.get(x);
				for (int y = x + 1; y < tags.size(); y++) {
					int tagIdy = tags.get(y);
					this.coocurenceMatrix.add(tagIdx, tagIdy, 1.0);
					this.coocurenceMatrix.add(tagIdy, tagIdx, 1.0);
				}			
			}*/
			
			for (int x = 0; x < tags.size(); x++) {
				int tagIdx = tags.get(x);
				for (int y = 0; y < tags.size(); y++) {
					//if (x==y)
						//continue;
					int tagIdy = tags.get(y);
					this.coocurenceMatrix.add(tagIdx, tagIdy, 1.0);
				}			
			}
		}
	}
	
	private void normalizeMatrix() {
		int x = 0;
		for (SparseVector vec : this.coocurenceMatrix) {
			int xCount = this.tagCounts.get(x);
			for (Map.Entry<Integer, Double> entry : vec.entrySet()) {
				int y = entry.getKey();
				int yCount = this.tagCounts.get(y);
				double coocurenceCount = entry.getValue().doubleValue();
				entry.setValue(coocurenceCount / (xCount + yCount - coocurenceCount) /** (double)(xCount + yCount) / (double)(xCount * yCount)*/);
			}
			x++;
		}
	}
	
	private void calculateRelatedness() {
		int sourceTag = 0;
		for (SparseVector vec : this.coocurenceMatrix) {
			int frequencySourceTag = this.tagCounts.get(sourceTag);
			for (Map.Entry<Integer, Double> entry : vec.entrySet()) {
				int destinationTag = entry.getKey();
				int frequencyDestinationTag = this.tagCounts.get(destinationTag);
				double coocurence = entry.getValue().doubleValue();
				entry.setValue((coocurence * (frequencySourceTag+frequencyDestinationTag)) / (frequencySourceTag*frequencyDestinationTag));
			}
			sourceTag++;
		}
	}
	
	// tags = sourceTags zur aktivierung
	public Map<Integer, Double> getCooccurenceTags(Map<Integer, Integer> tags) {
		Map<Integer, Double> resultTags = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Integer> sourceTag : tags.entrySet()) {
			SparseVector vec = this.coocurenceMatrix.get(sourceTag.getKey());
			for (Map.Entry<Integer, Double> coocurenceEntry : vec.entrySet()) {
				double weightedValue = sourceTag.getValue() * coocurenceEntry.getValue();
				//if (tagEntry.getKey() != entry.getKey() && weightedValue > 0.0) {
				if (weightedValue > 0.0) {
					Double tagVal = resultTags.get(coocurenceEntry.getKey());
					resultTags.put(coocurenceEntry.getKey(), tagVal == null ? weightedValue : tagVal.doubleValue() + weightedValue);
				}
			}
		}
		return resultTags;
	}
	
	public Map<Integer, Double> calculateAssociativeComponentsWithTagAssosiation(Map<Integer, Double> sourceTags, Map<Integer, Double> destinationTags, boolean srcCount, boolean destCount) {
		Map<Integer, Double> associativeComponents = new LinkedHashMap<Integer, Double>();				
		for (Map.Entry<Integer, Double> tag : sourceTags.entrySet()){
			associativeComponents.put(tag.getKey(), (srcCount ? tag.getValue() : 1.0) * this.calculateAssociativeComponent(tag.getKey(), destinationTags, destCount));
		}		
		return associativeComponents;
	}

	private Double calculateAssociativeComponent(int tag, Map<Integer, Double> destinationTags, boolean destCount) {
		SparseVector vec = this.coocurenceMatrix.get(tag);
		double associativeValue = 0.0;
		int numbAssociatedNodes = 0;
		for (Map.Entry<Integer, Double> destinationTag : destinationTags.entrySet()) {
			Double relatedness = vec.get(destinationTag.getKey());
			if (relatedness != null && relatedness > 0.0) {
				numbAssociatedNodes++;
				associativeValue += (relatedness * (destCount ? destinationTag.getValue() : 1.0));
			}

		}
		if (!destCount && numbAssociatedNodes > 0) {
			return associativeValue / numbAssociatedNodes;
		}
		return associativeValue;
	}

}
