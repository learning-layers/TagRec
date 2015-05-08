package processing.analyzing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import common.Bookmark;
import common.CooccurenceMatrix;

public class UserTagProperties {

	private Map<Integer, Integer> tagCounts = null;
	private Map<Integer, Integer> tagRecencies = null;
	private Map<Integer, Integer> tagContextSim = null;
	private Map<Integer, Integer> tagReuseProb = null;
	
	public UserTagProperties(List<Bookmark> userBookmarks, Bookmark testBookmark, CooccurenceMatrix tagMatrix) {
		this.tagCounts = new LinkedHashMap<Integer, Integer>();
		this.tagRecencies = new LinkedHashMap<Integer, Integer>();
		this.tagContextSim = new LinkedHashMap<Integer, Integer>();
		this.tagReuseProb = new LinkedHashMap<Integer, Integer>();
		
		// TODO: check for zero days
		for (Bookmark bookmark : userBookmarks) {
			int currentRecency = (int) Math.round((Long.parseLong(testBookmark.getTimestamp()) - Long.parseLong(bookmark.getTimestamp())) / 86400.0);
			for (int tag : bookmark.getTags()) {
				Integer count = this.tagCounts.get(tag);
				Integer recency = this.tagRecencies.get(tag);
				Integer contextSim = this.tagContextSim.get(tag);
				Integer reuseProb = this.tagReuseProb.get(tag);
				
				if (count == null) {
					this.tagCounts.put(tag, 1);
				} else {
					this.tagCounts.put(tag, count.intValue() + 1);
				}
				if (recency == null) {
					this.tagRecencies.put(tag, currentRecency);
				} else {
					if (currentRecency < recency) {
						this.tagRecencies.put(tag, currentRecency);
					}
				}
				if (tagMatrix != null) {
					if (contextSim == null) {
						this.tagContextSim.put(tag, tagMatrix.getCoocurenceCount(tag, testBookmark.getTags()));
					}
				}
				if (reuseProb == null) {
					this.tagReuseProb.put(tag, testBookmark.getTags().contains(tag) ? 1 : 0);
				}
			}
		}
	}
	
	public Map<Integer, Integer> getTagCounts() {
		return this.tagCounts;
	}

	public Map<Integer, Integer> getTagRecencies() {
		return this.tagRecencies;
	}
	
	public Map<Integer, Integer> getTagContextSim() {
		return this.tagContextSim;
	}
	
	public Map<Integer, Integer> getReuseProb() {
		return this.tagReuseProb;
	}
}
