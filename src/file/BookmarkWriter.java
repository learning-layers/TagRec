package file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import com.google.common.primitives.Ints;

import common.Bookmark;

public class BookmarkWriter {

	public static boolean writeSample(BookmarkReader reader, List<Bookmark> userSample, String filename, List<int[]> catPredictions, boolean realValues) {
		return doWriteSample(reader, userSample, null, filename, catPredictions, realValues);
	}
	
	public static boolean writeSample(BookmarkReader reader, List<Bookmark> userSample, String path, String filename, List<int[]> catPredictions, boolean realValues) {
		return doWriteSample(reader, userSample, path, filename, catPredictions, realValues);
	}

	private static boolean doWriteSample(BookmarkReader reader, List<Bookmark> userSample, String path, String filename, List<int[]> catPredictions, boolean realValues) {
		try {
			String filePath = "";
			if (path == null) {
				filePath = "./data/csv/" + filename + ".txt";
			} else {
				filePath = path + filename;
			}
			//FileWriter writer = new FileWriter(new File(filePath));
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(filePath)), "UTF8");
			BufferedWriter bw = new BufferedWriter(writer);
			int userCount = 0;
			// TODO: check encoding
			for (Bookmark bookmark : userSample) {
				String user = (realValues ? reader.getUsers().get(bookmark.getUserID()).replace("\"", "") : Integer.toString(bookmark.getUserID()));
				String resource = (realValues ? reader.getResources().get(bookmark.getResourceID()).replace("\"", "") : Integer.toString(bookmark.getResourceID()));
				bw.write("\"" + user + "\";");
				bw.write("\"" + resource + "\";");
				bw.write("\"" + bookmark.getTimestamp().replace("\"", "") + "\";\"");
				int i = 0;
				for (int t : bookmark.getTags()) {
					String tag = (realValues ? reader.getTags().get(t).replace("\"", "") : Integer.toString(t));
					bw.write(tag);
					if (++i < bookmark.getTags().size()) {
						bw.write(',');
					}					
				}
				bw.write("\";\"");
				
				List<Integer> userCats = (catPredictions == null ? bookmark.getCategories() : Ints.asList(catPredictions.get(userCount++)));
				i = 0;
				for (int cat : userCats) {
					//bw.write(URLEncoder.encode((catPredictions == null ? reader.getCategories().get(cat).replace("\"", "") : reader.getTags().get(cat)).replace("\"", ""), "UTF-8"));
					String catName = (realValues ? reader.getCategories().get(cat).replace("\"", "") : "t" + cat);
					bw.write(catName);
					if (++i < userCats.size()) {
						bw.write(',');
					}					
				}
				bw.write("\"");
				if (bookmark.getRating() != -2) {
					bw.write(";\"" + (int)bookmark.getRating() + "\"");
				} else {
					bw.write(";\"\"");
				}
				if (bookmark.getTitle() != null) {
					bw.write(";\"" + bookmark.getTitle() + "\"");
				} /*else {
					bw.write(";\"\"");
				}*/
				bw.write("\n");
			}
			
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
