package processing.analyzing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Bookmark;
import common.Utilities;
import file.BookmarkReader;

public class UserTagDistribution {

	public static void calculate(BookmarkReader reader, String dataset) {
		List<Integer> userSizes = new ArrayList<Integer>();
		List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(reader.getBookmarks(), false);
		for (List<Bookmark> userB : userBookmarks) {
			userSizes.add(userB.size());
		}
		Collections.sort(userSizes, Collections.reverseOrder());
		
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
