package processing.musicrec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import common.DoubleMapComparator;
import common.MapUtil;
import common.Bookmark;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.PredictionFileWriter;
import processing.BLLCalculator;
import file.BookmarkReader;

public class MusicCFRecommender {
	
	public static int MAX_NEIGHBORS = 20;
	
	private BookmarkReader reader;
	private List<Bookmark> trainList;
	private List<Bookmark> testList;
	private List<Map<Integer, Double>> userMaps;
	private Map<String, Double> simMap;
	private List<Map<Integer, Double>> bllMap;
	private Double beta;
	
	public MusicCFRecommender(BookmarkReader reader, int trainSize, Double bllVal, Double beta, String type) {
		this.reader = reader;
		this.beta = beta;
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		this.testList = this.reader.getBookmarks().subList(trainSize, this.reader.getBookmarks().size());
		this.simMap = new LinkedHashMap<String, Double>();
		
		if (bllVal != null) {
			this.bllMap = BLLCalculator.getArtifactMaps(this.reader, this.trainList, this.testList, false, 
					new ArrayList<Long>(), new ArrayList<Double>(), bllVal.doubleValue(), true, null);
			this.userMaps = getRecentUserArtists(30);
			//this.userMaps = getBLLUserArtists(30);
		} else {
			if (type.equals("general")) {
				this.userMaps = Utilities.getFloatUserMaps(this.trainList);
				System.out.println("Avg. number of genres per user: " + this.getAvgUserSize());
			} else if (type.equals("pop")) {
				this.userMaps = getTopUserArtists(30);
			} else {
				this.userMaps = getRecentUserArtists(30);
			}
		}
	}
	
	private List<Map<Integer, Double>> getTopUserArtists(int limit) {
		List<Map<Integer, Double>> returnList = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Integer> uMap : Utilities.getUserMaps(this.trainList)) {
			Map<Integer, Double> userArtists = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Integer> artist: MapUtil.sortByValue(uMap).entrySet()) {
				if (userArtists.size() < limit) {
					userArtists.put(artist.getKey(), (double)artist.getValue());
				} else {
					break;
				}
			}
			returnList.add(userArtists);
		}
		return returnList;
	}
	
	private List<Map<Integer, Double>> getBLLUserArtists(int limit) {
		List<Map<Integer, Double>> returnList = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Double> uMap : this.bllMap) {
			Map<Integer, Double> userArtists = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Double> artist: MapUtil.sortByValue(uMap).entrySet()) {
				if (userArtists.size() < limit) {
					userArtists.put(artist.getKey(), artist.getValue());
				} else {
					break;
				}
			}
			returnList.add(userArtists);
		}
		return returnList;
	}
	
	private List<Map<Integer, Double>> getRecentUserArtists(int limit) {
		List<Map<Integer, Double>> returnList = new ArrayList<Map<Integer, Double>>();
		List<List<Bookmark>> userBookmarks = Utilities.getBookmarks(this.trainList, false);
		for (List<Bookmark> bookmarks : userBookmarks) {
			Map<Integer, Double> uMap = new LinkedHashMap<Integer, Double>();
			int count = 0;
			int index = bookmarks.size() - 1;
			while (count < limit && index >= 0) {
				Bookmark b = bookmarks.get(index--);
				for (int t : b.getTags()) {
					if (!uMap.containsKey(t)) {
						uMap.put(t, (double)(limit - count++));
						if (count >= limit) {
							break;
						}
					}
				}
			}
			returnList.add(uMap);
		}	
		return returnList;
	}
	
	private double getAvgPairwiseSim() {
		double sim = 0.0;
		if (this.simMap.size() == 0) {
			return sim;
		}
		for (double s : this.simMap.values()) {
			sim += s;
		}
		return sim / this.simMap.size();
	}
	
	private double getAvgUserSize() {
		double size = 0.0;
		if (this.userMaps.size() == 0) {
			return size;
		}
		for (Map<Integer, Double> map : this.userMaps) {
			size += map.size();
		}
		return size / this.userMaps.size();
	}
	
	private void printPairwiseSim(String filename) {
		System.out.println("Entries to write: " + this.simMap.size());
		String fileToWrite = "./data/metrics/" + filename + "_cosine_sim.txt";
        try {
            FileWriter writer = new FileWriter(new File(fileToWrite));
            BufferedWriter bw = new BufferedWriter(writer);
			for (Map.Entry<String, Double> entry : this.simMap.entrySet()) {
				bw.write(entry.getKey() + ";");
				bw.write(entry.getValue() + "\n");
			}
			bw.flush();
			bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }     
	}
	
	private double getBetaForUser(Bookmark data) {
		if (this.beta < 0) {
			if (data.getRating() > 0) {
				return data.getRating();
			} else {
				System.out.println("Wrong rating");
				return 0.5;
			}
		} else {
			return this.beta.doubleValue();
		}
	}
	
	public Map<Integer, Double> getRankedTagList(Bookmark data, boolean sorting) {
		int userID = data.getUserID();
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		int i = 0;
		
		//Map<Integer, Integer> targetUserMap = this.userMaps.get(userID);
		Map<Integer, Double> neighbors = getNeighbors(userID);
		for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
			if (i++ < MAX_NEIGHBORS) {
				Map<Integer, Double> userMap = this.userMaps.get(entry.getKey());
				double simVal = entry.getValue().doubleValue();
				
				for (Map.Entry<Integer, Double> artist : userMap.entrySet()) {
					//Integer targetUserVal = targetUserMap.get(artist.getKey());
					//if (targetUserVal != null) {
						double artistSimVal = simVal * artist.getValue().doubleValue();// * targetUserVal.doubleValue();
						Double val = resultMap.get(artist.getKey());
						resultMap.put(artist.getKey(), (val != null ? val.doubleValue() + artistSimVal : artistSimVal));
					//}
				}
			} else {
				break;
			}
		}
		if (this.beta != null && userID < this.bllMap.size()) { // hybrid!
			double beta = getBetaForUser(data);
			MapUtil.normalizeMap(resultMap, beta);
			Map<Integer, Double> userBllMap = this.bllMap.get(userID);
			for (Map.Entry<Integer, Double> artist : userBllMap.entrySet()) {
				double bllval = artist.getValue().doubleValue() * (1.0 - beta);
				Double cfval = resultMap.get(artist.getKey());				
				double hybridval = (cfval != null ? cfval.doubleValue() + bllval : bllval);
				resultMap.put(artist.getKey(), hybridval);
			}
		}
		if (sorting) {
			Map<Integer, Double> sortedResultMap = MapUtil.sortByValue(resultMap);
			//Map<Integer, Double> sortedResultMap = new TreeMap<Integer, Double>(new DoubleMapComparator(resultMap));
			//sortedResultMap.putAll(resultMap);			
			Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
			int index = 0;
			for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
				if (index++ < Utilities.REC_LIMIT) {
					returnMap.put(entry.getKey(), entry.getValue().doubleValue());
				} else {
					break;
				}
			}
			return returnMap;
		}
		return resultMap;
	}
	
	private Map<Integer, Double> getNeighbors(int userID) {
		Map<Integer, Double> neighbors = new LinkedHashMap<Integer, Double>();
		// get all users 
		for (Bookmark data : this.testList) {
			if (data.getUserID() != userID) {
				neighbors.put(data.getUserID(), 0.0);
			}
		}
		
		if (userID < this.userMaps.size()) {
			Map<Integer, Double> targetMap = this.userMaps.get(userID);			
			for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
				double simVal = 0.0;
				String simID = (userID < entry.getKey() ? userID + ";" + entry.getKey() : entry.getKey() + ";" + userID);
				if (this.simMap.containsKey(simID)) {
					simVal = this.simMap.get(simID);
				} else {
					simVal = Utilities.getCosineFloatSim(targetMap, this.userMaps.get(entry.getKey()));
					//simVal = Utilities.getJaccardFloatSim(targetMap, this.userMaps.get(entry.getKey()));
					this.simMap.put(simID, simVal);
				}
				entry.setValue(simVal);
			}
			
			Map<Integer, Double> sortedNeighbors = MapUtil.sortByValue(neighbors);
			//Map<Integer, Double> sortedNeighbors = new TreeMap<Integer, Double>(new DoubleMapComparator(neighbors));
			//sortedNeighbors.putAll(neighbors);
			return sortedNeighbors;
		}
		System.out.println("Wrong user id");
		return neighbors;
	}
	
	// Statics --------------------------------------------------------------------------------------------------------
	
	private static List<Map<Integer, Double>> startCollaborativeFiltering(BookmarkReader reader, int sampleSize, String filename, Double bllVal, Double beta, String type) {
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;

		MusicCFRecommender calculator = new MusicCFRecommender(reader, trainSize, bllVal, beta, type);		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		for (int i = trainSize; i < size; i++) {
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = null;
			map = calculator.getRankedTagList(data, true);
			results.add(map);
		}
		System.out.println("Average pairwise sim: " + calculator.getAvgPairwiseSim());
		calculator.printPairwiseSim(filename);
		
		return results;
	}	
	
	public static BookmarkReader predictTags(String filename, int trainSize, int sampleSize, int neighbors, Double bllVal, Double beta, String type) {
		MAX_NEIGHBORS = neighbors;
		return predictSample(filename, trainSize, sampleSize, bllVal, beta, type);
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize, Double bllVal, Double beta, String type) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<Map<Integer, Double>> cfValues = startCollaborativeFiltering(reader, sampleSize, filename, bllVal, beta, type);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < cfValues.size(); i++) {
			Map<Integer, Double> modelVal = cfValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		String suffix = "_cf_" + type + "_";
		if (beta != null) {
			suffix = "_bll_cf_";
		}
        if (beta != null && beta > 0) {
        	suffix += "static_";
        }
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		String outputFile = filename + suffix + MAX_NEIGHBORS;
		writer.writeFile(outputFile);

		return reader;
	}
}
