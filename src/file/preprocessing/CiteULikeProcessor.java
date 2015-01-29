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

public class CiteULikeProcessor {

	public static boolean processFile(String inputFile, String outputFile, boolean filter) {
		try {
			FileReader reader = new FileReader(new File("./data/csv/" + inputFile));
			FileWriter writer = new FileWriter(new File("./data/csv/" + outputFile));
			BufferedReader br = new BufferedReader(reader);
			BufferedWriter bw = new BufferedWriter(writer);
			String line = null;
			String resID = "", userHash = "", timestamp = "";
			List<String> tags = new ArrayList<String>();
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\\|");
				String tag = lineParts[3];
				if (!filter || (!tag.contains("no-tag") && !tag.contains("-import"))) {
					if (!resID.isEmpty() && !userHash.isEmpty() && (!resID.equals(lineParts[0]) || !userHash.equals(lineParts[1]))) {
						writeLine(bw, resID, userHash, timestamp, tags);
						tags.clear();
					}
					resID = lineParts[0];
					userHash = lineParts[1];
					timestamp = lineParts[2];
					tags.add(tag);
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
		timestamp = timestamp.substring(0, timestamp.lastIndexOf("+"));
		return Timestamp.valueOf(timestamp).getTime() / 1000; // because of seconds
	}
}