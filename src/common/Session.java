package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Session {
	public int userID;
	public String startTime;


	private List<Bookmark> bookmarks;
	private HashSet<Integer> resources;
	public int lastResource;
	
	
	public Session() {
		this.resources = new HashSet<Integer>();
		this.bookmarks = new ArrayList<Bookmark>();
	}

	public void addBookmark(Bookmark bm) {
		bookmarks.add(bm);
		userID = bm.getUserID();
		startTime = bm.getTimestamp();
		bookmarks.add(bm);
		resources.add(bm.getWikiID());
		this.lastResource = bm.getWikiID();
	}

	public HashSet<Integer> getResources() {
		return resources;
	}

		
}
