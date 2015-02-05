package file.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BibsonomyProcessor {

	public static boolean processFile(String inputFile, String outputFile) {
		try {
			readBibtexFile();
			readBookmarkFile();
			
			FileReader reader = new FileReader(new File("./data/csv/bib_core/" + inputFile));
			FileWriter writer = new FileWriter(new File("./data/csv/bib_core/" + outputFile));
			BufferedReader br = new BufferedReader(reader);
			BufferedWriter bw = new BufferedWriter(writer);
			String line = null;
			String resID = "", userHash = "", timestamp = "";
			Set<String> tags = new HashSet<String>();
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				String tag = lineParts[1].toLowerCase();
				String type = lineParts[3];
				if (type.equals("2") && !tag.isEmpty() && !tag.equals("no-tag") && !tag.contains("-import") && !tag.contains("-export") && !tag.contains("sys:") && !tag.contains("system:") && !tag.contains("imported")) {
					if (!resID.isEmpty() && !userHash.isEmpty() && (!resID.equals(lineParts[2]) || !userHash.equals(lineParts[0]))) {
						if (resID != null) {
							BibBookmark bookmark = (type.equals("1") ? getBookmark(resID) : getBibtex(resID));
							if (bookmark != null) {
								writeLine(bw, bookmark.urlHash, userHash, timestamp, tags, bookmark);
							}
						}
						tags.clear();
					}
					resID = lineParts[2];
					userHash = lineParts[0];
					timestamp = lineParts[4];
					tags.add(tag);
				}
			}
			writeLine(bw, resID, userHash, timestamp, tags, null);
			
			br.close();
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean processUnsortedFile(String inputFile, String outputFile) {
		readBookmarkFile();
		readBibtexFile();
		int lineCount = 0;
		
		Map<String, Integer> resources = new LinkedHashMap<String, Integer>();
		Map<String, Set<String>> tagMap = new LinkedHashMap<String, Set<String>>();
		List<String> timestamps = new ArrayList<String>();
		List<BibBookmark> bookmarks = new ArrayList<BibBookmark>();
		
		try {			
			FileInputStream reader = new FileInputStream(new File("./data/csv/bib_core/" + inputFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(reader));
			
			FileOutputStream writer = new FileOutputStream(new File("./data/csv/bib_core/" + outputFile + ".txt"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(writer));
			
			String line = null;
			BibBookmark bookmark = null;
			String resID = "", userHash = "", timestamp = "", tag = "", type = "";
			
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				type = lineParts[3];
				//if (!type.equals("2")) { // skip bookmark or bibtex!
				//	continue;
				//}
				lineCount++;
				
				timestamp = lineParts[4];
				userHash = lineParts[0];
				if (type.equals("1")) {
					bookmark = getBookmark(lineParts[2]);
					if (bookmark == null) {
						continue;
					}
					resID = bookmark.urlHash + "1";
					if (resources.containsKey(resID)) {
						resID = resources.get(resID).toString();
					} else {
						int id = Integer.parseInt(lineParts[2]);
						resources.put(resID, id);
						resID = Integer.toString(id);
					}
				} else if (type.equals("2")) {
					bookmark = getBibtex(lineParts[2]);
					if (bookmark == null) {
						continue;
					}
					resID = bookmark.urlHash + "2";
					if (resources.containsKey(resID)) {
						resID = resources.get(resID).toString();
					} else {
						int id = Integer.parseInt(lineParts[2]);
						resources.put(resID, id);
						resID = Integer.toString(id);
					}
				}
				
				if (resID != null) {
					tag = lineParts[1].toLowerCase();
					Set<String> tags = tagMap.get(userHash + "_" + resID);
					if (tags == null) {
						tags = new HashSet<String>();
						tagMap.put(userHash + "_" + resID, tags);
						timestamps.add(timestamp);
						bookmarks.add(bookmark);
					}
					if (!tag.isEmpty() && !tag.equals("no-tag") && !tag.contains("-import") && !tag.contains("-export") && !tag.contains("sys:") && !tag.contains("system:") && !tag.contains("imported")) {
						tags.add(tag);
					}
				}
			}
			
			int i = 0;
			for (Map.Entry<String, Set<String>> entry : tagMap.entrySet()) {
				Set<String> tags = entry.getValue();
				if (tags.size() > 0) {
					String[] parts = entry.getKey().split("_");
					userHash = parts[0];
					resID = parts[1];
					timestamp = timestamps.get(i);
					bookmark = bookmarks.get(i);
					i++;
					writeLine(bw, resID, userHash, timestamp, tags, null/*bookmark*/); // do not use content here!
				}
			}
			
			br.close();
			bw.flush();
			bw.close();
			
			System.out.println("TAS with bib: " + lineCount);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static boolean writeLine(BufferedWriter bw, String resID, String userHash, String timestamp, Set<String> tags, BibBookmark bookmark) {
		try {
			String tagString = "";
			for (String tag : tags) {
				tagString += (tag + ",");
			}
			tagString = tagString.length() > 0 ? tagString.substring(0, tagString.length() - 1) : "";
			
			bw.write("\"" + userHash + "\";\"" + resID + "\";\"" + processTimestamp(timestamp) + "\";\"" + tagString + "\";\"\";\"\"");
			if (bookmark != null) {
				bw.write(";\"" + bookmark.url + "\";\"" + bookmark.desc + (bookmark.extDesc != "" ? (" " + bookmark.extDesc) : "")+ "\"");
			}
			bw.write("\n");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static long processTimestamp(String timestamp) {
		return Timestamp.valueOf(timestamp).getTime() / 1000; // because of seconds
	}
	
	private static Map<String, BibBookmark> bookmarkFile;
	
	private static void readBookmarkFile() {
		bookmarkFile = new LinkedHashMap<String, BibBookmark>();
		String line = null;
		try {
			FileReader bookmarkReader = new FileReader(new File("./data/csv/bib_core/bookmark"));
			BufferedReader bookmarkBr = new BufferedReader(bookmarkReader);
			while ((line = bookmarkBr.readLine()) != null) {
				String[] lineParts = line.split("\t");
				BibBookmark bookmark = new BibBookmark();
				if (lineParts.length >= 2) {
					bookmark.urlHash = lineParts[1];
					if (lineParts.length >= 3) {
						bookmark.url = lineParts[2].replace("\n", "").replace(";", "").replace("\r", "");
						if (lineParts.length >= 4) {
							bookmark.desc = lineParts[3].replace("\n", "").replace(";", "").replace("\r", "");
							if (lineParts.length >= 5) {
								bookmark.extDesc = lineParts[4].replace("\n", "").replace(";", "").replace("\r", "");
							}
						}
					}
					bookmarkFile.put(lineParts[0], bookmark);
				}
			}
			bookmarkBr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Map<String, BibBookmark> bibtexFile;
	
	private static void readBibtexFile() {
		bibtexFile = new LinkedHashMap<String, BibBookmark>();
		String line = null;
		int lineCount = 0;
		try {
			FileReader bookmarkReader = new FileReader(new File("./data/csv/bib_core/bibtex"));
			BufferedReader bookmarkBr = new BufferedReader(bookmarkReader);
			while ((line = bookmarkBr.readLine()) != null) {
				lineCount++;
				String[] lineParts = line.split("\t");
				BibBookmark bookmark = new BibBookmark();
				if (lineParts.length >= 29) {
					bookmark.urlHash = lineParts[28];
					if (lineParts.length >= 19) {
						bookmark.desc = lineParts[18].replace("\n", "").replace(";", "").replace("\r", "");
						if (bookmark.desc.equals("\\N")) {
							bookmark.desc = "";
						}
						if (lineParts.length >= 27) {
							bookmark.extDesc = lineParts[26].replace("\n", "").replace(";", "").replace("\r", "");
							if (bookmark.extDesc.equals("\\N")) {
								bookmark.extDesc = "";
							}
							if (lineParts.length >= 32) {
								bookmark.url = lineParts[31].replace("\n", "").replace(";", "").replace("\r", "");
							}
							if (bookmark.url.equals("\\N")) {
								bookmark.url = "";
							}
						}
					}
					bibtexFile.put(lineParts[0], bookmark);
				}
			}
			bookmarkBr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Bibtex lines: " + lineCount);
	}
	
	private static BibBookmark getBookmark(String resID) {
		if (bookmarkFile != null) {
			return bookmarkFile.get(resID);
		} else {
			return null;
		}
	}
	
	private static BibBookmark getBibtex(String resID) {
		if (bibtexFile != null) {
			return bibtexFile.get(resID);
		} else {
			return null;
		}
	}
}