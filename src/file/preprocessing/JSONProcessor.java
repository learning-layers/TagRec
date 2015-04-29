package file.preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.Bookmark;
import common.Utilities;
import file.BookmarkReader;

public class JSONProcessor {

	public static void writeJSONOutput(String filename) {
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(filename);
		Set<Integer> resources = new HashSet<Integer>();
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(new File("./data/csv/" + filename + ".json"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(writer));		
		
		for (Bookmark bookmark : reader.getBookmarks()) {
			//if (!resources.contains(bookmark.getResourceID())) {
				JSONObject jsonOutput = new JSONObject();	
				jsonOutput.put("url", reader.getResources().get(bookmark.getResourceID()));
				jsonOutput.put("timestamp", new Integer(bookmark.getTimestamp()));
				JSONArray jsonTags = new JSONArray();
				for (Integer tag : bookmark.getTags()) {
					jsonTags.add(reader.getTags().get(tag));
				}
				jsonOutput.put("tags", jsonTags);
				resources.add(bookmark.getResourceID());
				try {
					bw.write(jsonOutput.toJSONString() + "\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			//}
		}
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
