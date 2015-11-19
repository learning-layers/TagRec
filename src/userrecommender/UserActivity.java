package userrecommender;

/**
 * User activity with a time stamp in milisec for a given resource.
 * 
 * @author ilire.mavriqi
 *
 */
public class UserActivity implements Comparable<UserActivity> {
	private int resourceId = 0;
	private int userId = 0;
	private long timeStamp = 0;
	
	public UserActivity() {
		super();
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public int compareTo(UserActivity activity) {
		int result = -2;
		if (this.timeStamp < activity.getTimeStamp()) {
			result = -1;
		}
		if (this.timeStamp >= activity.getTimeStamp()) {
			result = 1;
		}
		return result;
	}	
}
