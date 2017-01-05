package processing.hashtag.social;

import java.util.Date;


/**
 * @author spujari
 * 
 *
 */
public class RelationItem {
	private int id;
	private String targetUser;
	private String initUser;
	private long tweetId;
	private Date createdAt;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTargetUser() {
		return targetUser;
	}
	public void setTargetUser(String target_user) {
		this.targetUser = target_user;
	}
	public String getInitUser() {
		return initUser;
	}
	public void setInitUser(String init_user) {
		this.initUser = init_user;
	}
	public long getTweetId() {
		return tweetId;
	}
	public void setTweetId(long tweetId) {
		this.tweetId = tweetId;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
