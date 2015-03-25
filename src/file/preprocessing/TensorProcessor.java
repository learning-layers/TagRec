package file.preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Bookmark;
import common.Utilities;

import file.BookmarkReader;
import file.postprocessing.CatDescFiltering;

public class TensorProcessor {

	private static Set<String> entries;
	
	public static void writeFiles(String filename, int trainSize, int testSize, boolean tagRec, Integer minBookmarks, Integer maxBookmarks, CatDescFiltering filter) {
		entries = new HashSet<String>();
		//filename += "_res";

		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);	
		List<Bookmark> trainList = reader.getBookmarks().subList(0, trainSize);
		List<Bookmark> testList = reader.getBookmarks().subList(trainSize, trainSize + testSize);
		String name = "";//(tagRec ? "_tensor" : "_mymedialite");
		String outputFilename = filename.split("_")[0];
		// train file
		// TODO: reader
		createFile(trainList, "./data/csv/" + outputFilename + "_train" + name + ".txt", null, false, tagRec, minBookmarks, maxBookmarks, null);
		// test file
		String suffix = "";
		if (filter != null) {
			suffix += ("_" + (filter.getDescriber() ? "desc" : "cat"));
		}
		createFile(testList, "./data/csv/" + outputFilename + suffix + "_test" + name + ".txt", null, true, tagRec, minBookmarks, maxBookmarks, filter);
	}
	
	private static void createFile(List<Bookmark> list, String filename, BookmarkReader reader, boolean testset, boolean tagRec, Integer minBookmarks, Integer maxBookmarks, CatDescFiltering filter) {
		try {
			File tempFile = new File(filename);
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
			for (Bookmark data : list) {
				if (testset && reader != null) { // means test-set
					// TODO: check for resource
					if (!Utilities.isEntityEvaluated(reader, data.getUserID(), minBookmarks, maxBookmarks, false)) {
						continue; // skip this user if it shoudln't be evaluated
					}
				}
				if (filter != null) { // also for test-set
					if (!filter.evaluate(data.getUserID())) {
						continue;
					}
				}
					
				if (!entries.contains(data.getUserID() + "_" + data.getWikiID())) {
					if (tagRec) {
						for (int tag : data.getTags()) {
							bw.write(data.getUserID() + "\t" + data.getWikiID() + "\t" + tag + "\n");
						}
					} else {
						bw.write(data.getUserID() + "\t" + (reader == null ? data.getWikiID() : reader.getResources().get(data.getWikiID())) + "\n");
					}
					entries.add(data.getUserID() + "_" + data.getWikiID());
				}
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
