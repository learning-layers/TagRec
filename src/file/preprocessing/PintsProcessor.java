package file.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PintsProcessor {

	// used for datasets from PINTS: Delicious (big) and Flickr
	public static boolean processFile(String dir, String inputFile, String outputFile) {
		
		Map<String, Set<String>> tagMap = new LinkedHashMap<String, Set<String>>();
		List<String> timestamps = new ArrayList<String>();
		
		try {
			FileReader reader = new FileReader(new File("./data/csv/" + dir + "/" + inputFile));
			FileWriter writer = new FileWriter(new File("./data/csv/" + dir + "/" + outputFile + ".txt"));
			BufferedReader br = new BufferedReader(reader);
			BufferedWriter bw = new BufferedWriter(writer);
			String line = null;
			String resID = "", userHash = "", timestamp = "", tag = "";
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length < 4) {
					continue;
				}
				timestamp = lineParts[0];
				userHash = lineParts[1];
				resID = lineParts[2];
				tag = lineParts[3].toLowerCase();
				if (!(!tag.isEmpty() && !tag.equals("no-tag") && !tag.contains("-import") && !tag.contains("-export") && !tag.contains("sys:") && !tag.contains("system:") && !tag.contains("imported"))) {
					continue;
				}
				Set<String> tags = tagMap.get(userHash + "_" + resID);
				if (tags == null) {
					tags = new LinkedHashSet<String>();
					tagMap.put(userHash + "_" + resID, tags);
					timestamps.add(timestamp);
					if (timestamps.size() % 100000 == 0) {
						System.out.println("READ 100000 bookmarks");
					}
				}
				tags.add(tag);
			}
			
			int i = 0;
			for (Map.Entry<String, Set<String>> entry : tagMap.entrySet()) {
				Set<String> tags = entry.getValue();
				String[] parts = entry.getKey().split("_");
				userHash = parts[0];
				resID = parts[1];
				timestamp = timestamps.get(i++);
				writeLine(bw, resID, userHash, timestamp, tags);
				if (i % 100000 == 0) {
					System.out.println("WROTE 100000 bookmarks");
				}
			}
			
			br.close();
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static boolean writeLine(BufferedWriter bw, String resID, String userHash, String timestamp, Set<String> tags) {
		try {
			if (tags.size() == 0) {
				return false;
			}
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
		return Timestamp.valueOf(timestamp).getTime() / 1000; // because of seconds
	}
}