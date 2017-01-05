package processing.hashtag.solr;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Tweet implements Serializable {

	private String id;
	private String userid;
	private String text;
	private String timestamp;
	private Set<String> hashtags = new LinkedHashSet<String>();
	
	public Tweet(String id, String userid, String text, String timestamp, Set<String> hashtags) {
		this.id = id;
		this.userid = userid;
		this.text = text;
		this.timestamp = timestamp;
		for (String ht : hashtags) {
			this.hashtags.add(ht.toLowerCase());
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Set<String> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<String> hashtags) {
		this.hashtags = hashtags;
	}
}
