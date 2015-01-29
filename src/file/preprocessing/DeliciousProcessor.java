package file.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DeliciousProcessor {

	public static boolean processFile(String inputFile, String outputFile) {
		try {
			FileReader reader = new FileReader(new File("./data/csv/" + inputFile));
			FileWriter writer = new FileWriter(new File("./data/csv/" + outputFile));
			BufferedReader br = new BufferedReader(reader);
			BufferedWriter bw = new BufferedWriter(writer);
			String line = null;
			String resID = "", userHash = "", timestamp = "";
			List<String> tags = new ArrayList<String>();
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length >= 4) {
					String tag = lineParts[3].toLowerCase();
					if (!tag.isEmpty() && !tag.equals("no-tag") && !tag.contains("-import") && !tag.equals("imported")) {
						if (!resID.isEmpty() && !userHash.isEmpty() && (!resID.equals(lineParts[2]) || !userHash.equals(lineParts[1]))) {
							writeLine(bw, resID, userHash, timestamp, tags);
							tags.clear();
						}
						resID = lineParts[2];
						userHash = lineParts[1];
						timestamp = lineParts[0];
						tags.add(tag);
					}
				}
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
		//if (tags.size() > 1) {
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
		//}
		return false;
	}
	
	private static long processTimestamp(String timestamp) {
		return Timestamp.valueOf(timestamp).getTime() / 1000; // because of seconds
	}
}