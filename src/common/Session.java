package common;

import java.util.ArrayList;
import java.util.List;

public class Session {
	private int userID;
	private String startTime;


	private List<Bookmark> bookmarks;
	private List<Integer> resources;
	private List<Integer> tags;
	private List<Integer> categories;
	
	public Session() {
		this.categories = new ArrayList<Integer>();
		this.tags = new ArrayList<Integer>();
		this.resources = new ArrayList<Integer>();
		this.bookmarks = new ArrayList<Bookmark>();
	}

	public void addBookmark(Bookmark bm) {
		bookmarks.add(bm);
		userID = bm.getUserID();
		startTime = bm.getTimestamp();
		bookmarks.add(bm);
		resources.add(bm.getWikiID());
		tags.addAll(bm.getTags());
		categories.addAll(bm.getCategories());
	}

	public List<Integer> getResources() {
		return resources;
	}

	public List<Integer> getTags() {
		return tags;
	}
	
}
