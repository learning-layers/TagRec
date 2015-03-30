/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package processing;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import common.Bookmark;
import common.MemoryThread;
import common.PerformanceMeasurement;
import common.Utilities;
import file.PredictionFileWriter;
import file.BookmarkReader;
import processing.folkrank.*;

public class FolkRankCalculator {
		
	private static List<int[]> frResults;
	private static List<int[]> prResults;
	private static String timeString;
	
	private static void startFolkRankCreation(BookmarkReader reader, int sampleSize) {
		System.out.println("\nStart FolkRank Calculation for Tags");
		frResults = new ArrayList<int[]>();
		prResults = new ArrayList<int[]>();
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		Stopwatch timer = new Stopwatch();
		timer.start();
		FactReader factReader = new WikipediaFactReader(reader, trainSize, 3);
		FactPreprocessor prep = new FactReaderFactPreprocessor(factReader);
		prep.process();
		FolkRankData facts = prep.getFolkRankData();
		
        FolkRankParam param = new FolkRankParam();
        FolkRankPref pref = new FolkRankPref(new double[] {1.0, 1.0, 1.0});
        int usrCounts = facts.getCounts()[1].length;
        System.out.println("Users: " + usrCounts);
        int resCounts = facts.getCounts()[2].length;
        System.out.println("Resources: " + resCounts);
        double[][] prefWeights = new double[][]{new double[]{}, new double[]{usrCounts}, new double[]{resCounts}};      
        FolkRankAlgorithm folk = new FolkRankAlgorithm(param);
        timer.stop();
        long trainingTime = timer.elapsed(TimeUnit.MILLISECONDS);
        
		timer.reset();
        // start FolkRank        
		for (int i = trainSize; i < size; i++) {
			timer.start();
			Bookmark data = reader.getBookmarks().get(i);
	        int u = data.getUserID();
	        int[] uPrefs = (u < usrCounts ? new int[]{u} : new int[]{});
	        int r = data.getResourceID();
	        int[] rPrefs = (r < resCounts ? new int[]{r} : new int[]{});
	        pref.setPreference(new int[][]{new int[]{}, uPrefs, rPrefs}, prefWeights);
	        FolkRankResult result = folk.computeFolkRank(facts, pref);
	        
			int[] topTags = new int[10];
	        SortedSet<ItemWithWeight> topKTags = ItemWithWeight.getTopK(facts, result.getWeights(), 10, 0);
	        int count = 0;
	        for (ItemWithWeight item : topKTags) {
	            topTags[count++] = item.getItem();
	        }
	        frResults.add(topTags);
	        timer.stop();
	        
	        int[] topTagsPr = new int[10];
	        SortedSet<ItemWithWeight> topKTagsPr = ItemWithWeight.getTopK(facts, result.getAPRWeights(), 10, 0);
	        count = 0;
	        for (ItemWithWeight item : topKTagsPr) {
	            topTagsPr[count++] = item.getItem();
	        }
	        prResults.add(topTagsPr);
	        //System.out.println(u + "|" + data.getTags().toString().replace("[", "").replace("]", "") + 
	        //					   "|" + Arrays.toString(topTags).replace("[", "").replace("]", "") + 
	        //					   "|" + Arrays.toString(topTagsPr).replace("[", "").replace("]", ""));
		}
		long testTime = timer.elapsed(TimeUnit.MILLISECONDS);
		
		timeString = PerformanceMeasurement.addTimeMeasurement(timeString, true, trainingTime, testTime, sampleSize);
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize) {
		Timer timerThread = new Timer();
		MemoryThread memoryThread = new MemoryThread();
		timerThread.schedule(memoryThread, 0, MemoryThread.TIME_SPAN);
		
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);
		List<int[]> predictionValues = null;
		List<int[]> prPredictionValues = null;
		startFolkRankCreation(reader, sampleSize);
		predictionValues = frResults;
		prPredictionValues = prResults;
		
		reader.setTestLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		writer.writeFile(filename + "_fr");
		PredictionFileWriter prWriter = new PredictionFileWriter(reader, prPredictionValues);
		prWriter.writeFile(filename + "_apr");
		
		timeString = PerformanceMeasurement.addMemoryMeasurement(timeString, false, memoryThread.getMaxMemory());
		timerThread.cancel();
		Utilities.writeStringToFile("./data/metrics/" + filename + "_fr" + "_TIME.txt", timeString);
		return reader;
	}
}
