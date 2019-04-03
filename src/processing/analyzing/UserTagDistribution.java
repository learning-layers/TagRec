package processing.analyzing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Bookmark;
import common.Utilities;
import file.BookmarkReader;

public class UserTagDistribution {

	public static void calculate(BookmarkReader reader, String dataset) {
		List<Integer> userSizes = new ArrayList<Integer>();
		List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(reader.getBookmarks(), false);
		double reuseRatio = 0.0;
		for (List<Bookmark> userB : userBookmarks) {
			Set<Integer> userResources = new HashSet<Integer>();
			double reuseCount = 0.0;
			for (Bookmark b : userB) {
				if (userResources.contains(b.getResourceID())) {
					reuseCount++;
				} else {
					userResources.add(b.getResourceID());
				}
			}
			userSizes.add(userB.size());
			reuseRatio += (reuseCount / userB.size());
		}
		Collections.sort(userSizes, Collections.reverseOrder());
		System.out.println("Resource reuse ratio: " + reuseRatio / userBookmarks.size());
		
		try {
			FileWriter userWriter = new FileWriter(new File("./data/csv/" + dataset + "_userDist.txt"));
			BufferedWriter userBW = new BufferedWriter(userWriter);
			for (int size : userSizes) {
				userBW.write(size + "\n");
			}
			userBW.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
