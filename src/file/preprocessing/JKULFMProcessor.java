package file.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JKULFMProcessor {
	
	private static final String EVENTS_FILE = "./data/schedl/LFM-1b_LEs.txt";
	
	private static Set<String> getFilterUsers(String filterFile) throws Exception {
		Set<String> filterUsers = new HashSet<String>();
		InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(filterFile)), "UTF8");
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		int i = 0;
		while ((line = br.readLine()) != null) {
			if (i > 0) {
				String userID = line.substring(0, line.indexOf(','));
				filterUsers.add(userID);
			}
			i++;
		}
		System.out.println("Number of users: " + (i - 1));
		br.close();
		reader.close();
		
		return filterUsers;
	}
	
	private static List<String> getFilterLines(Set<String> filterUsers) throws Exception {
		String filePath = EVENTS_FILE;
		List<String> filterLines = new ArrayList<String>();
		
		InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF8");
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		int i = 0;
		while ((line = br.readLine()) != null) {
			String userID = line.substring(0, line.indexOf('\t'));
			if (filterUsers.contains(userID)) {
				filterLines.add(line);
				i++;
			}
		}
		System.out.println("Number of lines: " + i);
		br.close();
		reader.close();
		
		return filterLines;
	}
	
	private static void writeOutputFile(String outputFile, List<String> lines) throws Exception {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(outputFile)), "UTF8");
		BufferedWriter bw = new BufferedWriter(writer);
		
		int i = 0;
		for(String l : lines) {
			bw.write(l + '\n');
			i++;
		}
		System.out.println("Written lines: " + i);
		
		bw.flush();
		bw.close();
		writer.close();
	}
	
	public static void preprocessFile(String filterFile, String outputFile) {		
		try {
			// get filter users
			Set<String> filterUsers = getFilterUsers(filterFile);
			
			// read big file and filter user actions
			List<String> filterLines = getFilterLines(filterUsers);
			
			// write file
			writeOutputFile(outputFile, filterLines);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
