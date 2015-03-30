package userrecommender;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import common.Bookmark;
import common.DoubleMapComparator;
import file.BookmarkReader;

/**
 * Spear Algorithm wrapper
 * 
 * @author ilire.mavriqi
 *
 */

public class SpearCalculator {

	private static BookmarkReader reader = new BookmarkReader(0, false);

	public static void calculateScores(String inputDataFileName, String outputDirectory, int numberOfIterations) {

		reader.readFile(inputDataFileName);
		LinkedList<Integer> userList = getUsers();
	
		HashMap<Integer, TreeSet<UserActivity>> activities = getActivities();
				
		LinkedList<Integer> uniqueResources = new LinkedList<Integer>();
		uniqueResources.addAll(activities.keySet());
		
		SpearAlgorithm alg = new SpearAlgorithm(activities, userList, uniqueResources);
		
		SpearAlgorithmResult result = alg.execute(numberOfIterations);
		
		// sort results
		Map<Integer, Double> sortedExpertiseScores = new TreeMap<Integer, Double>(new DoubleMapComparator(result.getExpertiseResult()));
		sortedExpertiseScores.putAll(result.getExpertiseResult());
		
		Map<Integer, Double> sortedResourceScores = new TreeMap<Integer, Double>(new DoubleMapComparator(result.getQualityResult()));
		sortedResourceScores.putAll(result.getQualityResult());
		
		//TODO write in output directory
		
		for (Map.Entry<Integer, Double> entry : sortedExpertiseScores.entrySet()) {
			System.out.println ("Experts results:  User with ID : " + entry.getKey() + "  got the score => " +entry.getValue());
		}
		
		for (Map.Entry<Integer, Double>  entry : sortedResourceScores.entrySet()) {
			System.out.println ("Resource results:  Resource with ID : " + entry.getKey() + "  got the score => " +entry.getValue());
		}
	}

	private static LinkedList<Integer> getUsers() {
		LinkedList<Integer> userList = new LinkedList<Integer>();
		for (int i = 0; i < reader.getUsers().size(); i++) {
			userList.add(Integer.parseInt(reader.getUsers().get(i)));
		}
		return userList;
	}

	private static HashMap<Integer, TreeSet<UserActivity>> getActivities() {
		HashMap<Integer, TreeSet<UserActivity>> activities = new HashMap<Integer, TreeSet<UserActivity>>();

		for (Bookmark bookmark : reader.getBookmarks()) {
			UserActivity activity = new UserActivity();
			activity.setUserId(bookmark.getUserID());
			activity.setResourceId(bookmark.getResourceID());
			activity.setTimeStamp(Long.parseLong(bookmark.getTimestamp()));

		
			if (activities.containsKey(bookmark.getResourceID())) {
				//already contains values, get list, add new value
				TreeSet<UserActivity> values = activities.get(bookmark.getResourceID());
				values.add(activity);
				activities.put(bookmark.getResourceID(), values);
			
			} else {
				TreeSet<UserActivity> userActivityTree = new TreeSet<UserActivity>();
				userActivityTree.add(activity);
				activities.put(bookmark.getResourceID(), userActivityTree);
			}
		}
		
		return activities;
	}
	
}
