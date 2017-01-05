package processing.hashtag.social;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.Bookmark;

/**
 * @author spujari
 * This class init the main datastructures used in program.
 */
public class SocialInitEngine {

	/**
	 * Get the underlying network over which the information flow.
	 * @param filepath
	 * @param nameIdMap
	 * @return HashMap name and friends as
	 **/
	public static HashMap<String, ArrayList<String>> getNetwork(String filepath, HashMap<String, Integer> nameIdMap) {
		HashMap<String, ArrayList<String>> network = new HashMap<String, ArrayList<String>>();
		try {
			File file = new File(filepath);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF8");
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("\t");
				String user1 = tokens[0];
				String user2 = tokens[1];
				if (!network.containsKey(user1)) {
					network.put(user1, new ArrayList<String>());
					network.get(user1).add(user2);
				} else {
					network.get(user1).add(user2);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find the network file: " + filepath + "\n");
		} catch (IOException e) {
			System.out.println("Error in reading from the network file: " + filepath + "\n");
		}
		return network;
	}

	/**
	 * The user tags and their timeline information.
	 * @param bookmarkList
	 * @return
	 */
	public static HashMap<String, HashMap<Integer, ArrayList<Long>>> getBookmarks(List<Bookmark> bookmarkList, List<String> users) {
		HashMap<String, HashMap<Integer, ArrayList<Long>>> userTagTimes = new HashMap<String, HashMap<Integer, ArrayList<Long>>>();
		for (Bookmark bookmark : bookmarkList) {
			List<Integer> taglist = bookmark.getTags();
			Integer userId = bookmark.getUserID();
			// get the userName for the id.
			String userName = users.get(userId);
			// userName = this.twitterScreenNameIdMap.get(userName);
			String timestamp = bookmark.getTimestamp();
			Long timestampLong = bookmark.getTimestampAsLong();
			if (!userTagTimes.containsKey(userName)) {
				userTagTimes.put(userName, new HashMap<Integer, ArrayList<Long>>());
			}
			for (Integer tag : taglist) {
				if (!userTagTimes.get(userName).containsKey(tag)) {
					userTagTimes.get(userName).put(tag, new ArrayList<Long>());
				}
				userTagTimes.get(userName).get(tag).add(timestampLong);
			}
		}
		return userTagTimes;
	}
	
	/**
	 * Takes a list of username string with user id as index of the list.
	 * 
	 * @param idNameMap
	 * @return {@link HashMap} the map from name of user to his id in bookmark
	 *         system.
	 */
	public static HashMap<String, Integer> getNameIdMap(List<String> idNameMap) {

		HashMap<String, Integer> nameIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < idNameMap.size(); i++) {
			nameIdMap.put(idNameMap.get(i), i);
		}
		return nameIdMap;
	}
}
