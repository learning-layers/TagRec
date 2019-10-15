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

public class ArtistCFRecommender {
	
	public static int MAX_NEIGHBORS = 20;
	
	private BookmarkReader reader;
	private List<Bookmark> trainList;
	private List<Bookmark> testList;
	private List<Map<Integer, Double>> userMaps;
	private Map<Integer, Map<Integer, Double>> artistGenreMap;
	private Map<Integer, Map<Integer, Double>> artistNeighborMap;
	
	public ArtistCFRecommender(BookmarkReader reader, int trainSize) {
		this.reader = reader;
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
		this.testList = this.reader.getBookmarks().subList(trainSize, this.reader.getBookmarks().size());
		this.artistNeighborMap = new LinkedHashMap<Integer, Map<Integer, Double>>();
		
		this.userMaps = getTopUserArtists(MAX_NEIGHBORS);//Utilities.getFloatUserMaps(this.trainList);
		this.artistGenreMap = getArtistGenreMap();
	}
	
	private List<Map<Integer, Double>> getTopUserArtists(int limit) {
		List<Map<Integer, Double>> returnList = new ArrayList<Map<Integer, Double>>();
		for (Map<Integer, Integer> uMap : Utilities.getUserResourceMaps(this.trainList)) {
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
	
	private Map<Integer, Map<Integer, Double>> getArtistGenreMap() {
		Map<Integer, Map<Integer, Double>> artistGenreMap = new LinkedHashMap<Integer, Map<Integer, Double>>();
		for (Bookmark b : this.trainList) {
			int a = b.getResourceID();
			if (!artistGenreMap.containsKey(a)) {
				Map<Integer, Double> gMap = new LinkedHashMap<Integer, Double>();
				for (int g : b.getTags()) {
					gMap.put(g, 1.0);
				}
				artistGenreMap.put(a, gMap);
			}
		}
		
		return artistGenreMap;
	}
	
	private Map<Integer, Double> getNeighbors(int artistID) {
		if (this.artistNeighborMap.containsKey(artistID)) {
			return artistNeighborMap.get(artistID);
		}
		
		Map<Integer, Double> neighbors = new LinkedHashMap<Integer, Double>();
		// get all artists
		for (int a = 0; a < this.reader.getResources().size(); a++) {
			if (a != artistID) {
				neighbors.put(a, 0.0);
			}
		}
		if (this.artistGenreMap.containsKey(artistID)) {
			Map<Integer, Double> targetMap = this.artistGenreMap.get(artistID);			
			for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
				if (this.artistGenreMap.containsKey(entry.getKey())) {
					double simVal = Utilities.getCosineFloatSim(targetMap, this.artistGenreMap.get(entry.getKey()));
					entry.setValue(simVal);
				}
			}			
			Map<Integer, Double> sortedNeighbors = MapUtil.sortByValue(neighbors);
			Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Double> entry : sortedNeighbors.entrySet()) {
				if (returnMap.size() < MAX_NEIGHBORS) {
					returnMap.put(entry.getKey(), entry.getValue().doubleValue());
				} else {
					break;
				}
			}
			this.artistNeighborMap.put(artistID, returnMap);
			return returnMap;
		}
		System.out.println("Wrong artist id");
		return neighbors;
	}
	
	// for the artists of a user, get neighbors and the genres of these neighbors
	public Map<Integer, Double> getRankedTagList(Bookmark data, boolean sorting) {
		int userID = data.getUserID();
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();		
		Map<Integer, Double> targetUserMap = null;
		if (Utilities.FILTER_OWN) {
			targetUserMap = this.userMaps.get(userID);
		} else {
			targetUserMap = new LinkedHashMap<Integer, Double>();
		}
		
		for (int artistID : this.userMaps.get(userID).keySet()) {
			Map<Integer, Double> neighbors = getNeighbors(artistID);
			for (Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
				Map<Integer, Double> neighborMap = this.artistGenreMap.get(entry.getKey());
				double simVal = entry.getValue().doubleValue();	
				for (Map.Entry<Integer, Double> genre : neighborMap.entrySet()) {
					double artistSimVal = simVal * genre.getValue().doubleValue();
					Double val = resultMap.get(genre.getKey());
					resultMap.put(genre.getKey(), (val != null ? val.doubleValue() + artistSimVal : artistSimVal));
				}
			}
		}
		if (sorting) {
			Map<Integer, Double> sortedResultMap = MapUtil.sortByValue(resultMap);			
			Map<Integer, Double> returnMap = new LinkedHashMap<Integer, Double>();
			for (Map.Entry<Integer, Double> entry : sortedResultMap.entrySet()) {
				if (returnMap.size() < Utilities.REC_LIMIT) {
					if (!targetUserMap.containsKey(entry.getKey())) {
						returnMap.put(entry.getKey(), entry.getValue().doubleValue());
					}
				} else {
					break;
				}
			}
			return returnMap;
		}
		return resultMap;
	}
		
	// Statics --------------------------------------------------------------------------------------------------------
	
	private static List<Map<Integer, Double>> startCollaborativeFiltering(BookmarkReader reader, int sampleSize, String filename) {
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;

		ArtistCFRecommender calculator = new ArtistCFRecommender(reader, trainSize);		
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		for (int i = trainSize; i < size; i++) {
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = null;
			map = calculator.getRankedTagList(data, true);
			results.add(map);
		}
		
		return results;
	}	
	
	public static BookmarkReader predictTags(String filename, int trainSize, int sampleSize, int neighbors) {
		MAX_NEIGHBORS = neighbors;
		return predictSample(filename, trainSize, sampleSize);
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		
		List<Map<Integer, Double>> cfValues = startCollaborativeFiltering(reader, sampleSize, filename);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < cfValues.size(); i++) {
			Map<Integer, Double> modelVal = cfValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}		
		String suffix = "_cfartist_";
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		String outputFile = filename + suffix + MAX_NEIGHBORS;
		writer.writeFile(outputFile);

		return reader;
	}
}
