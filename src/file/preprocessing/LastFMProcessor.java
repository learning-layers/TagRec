package file.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// used for LastFm and Delicious (small)
// userID	bookmarkID	tagID	timestamp
public class LastFMProcessor {

	public static boolean processFile(String inputFile, String outputFile) {
		try {
			FileReader reader = new FileReader(new File("./data/csv/lastfm_core/" + inputFile));
			FileWriter writer = new FileWriter(new File("./data/csv/lastfm_core/" + outputFile + ".txt"));
			BufferedReader br = new BufferedReader(reader);
			BufferedWriter bw = new BufferedWriter(writer);
			String line = null;
			String resID = "", userHash = "", tagID = "", timestamp = "";
			List<String> tags = new ArrayList<String>();
			
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (i++ == 0) { // skip first line
					continue;
				}
				String[] lineParts = line.split("\t");
				if (!resID.isEmpty() && !userHash.isEmpty() && (!resID.equals(lineParts[1]) || !userHash.equals(lineParts[0]))) {
					//resID = getNameByID(resID, "resources");
					if (resID != null) {
						writeLine(bw, resID, userHash, timestamp, tags);
					}
					tags.clear();
				}
				tagID = lineParts[2];
				resID = lineParts[1];
				userHash = lineParts[0];
				timestamp = lineParts[3];
				String tagName = getNameByID(tagID, "tags.dat");
				if (tagName != null) {
					tags.add(tagName);
				}
				//tags.add(tagID);
			}
			writeLine(bw, resID, userHash, timestamp, tags);
			
			br.close();
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static boolean writeLine(BufferedWriter bw, String resID, String userHash, String timestamp, List<String> tags) {
		try {
			String tagString = "";
			for (String tag : tags) {
				tagString += (tag + ",");
			}
			tagString = tagString.length() > 0 ? tagString.substring(0, tagString.length() - 1) : "";
			
			bw.write("\"" + userHash + "\";\"" + resID + "\";\"" + processTimestamp(timestamp) + "\";\"" + tagString + "\";\"\"\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static long processTimestamp(String timestamp) {
		return Long.parseLong(timestamp) / 1000; // because of seconds
	}
	
	private static String getNameByID(String id, String file) {
		String line = null;
		try {
			FileReader bookmarkReader = new FileReader(new File("./data/csv/lastfm_core/" + file));
			BufferedReader bookmarkBr = new BufferedReader(bookmarkReader);
			while ((line = bookmarkBr.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length >= 2 && lineParts[0].equals(id)) {
					bookmarkBr.close();
					return lineParts[1];
				}
			}
			bookmarkBr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
