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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.common.primitives.Ints;

import file.PredictionFileWriter;
import file.BookmarkReader;
import common.Bookmark;

public class ContentBasedCalculator {

	private final static int REC_LIMIT = 10;
	
	private BookmarkReader reader;
	private List<Bookmark> trainList;
	
	public ContentBasedCalculator(BookmarkReader reader, int trainSize) {
		this.reader = reader;
		
		// TODO: use this data for recommendations
		this.trainList = this.reader.getBookmarks().subList(0, trainSize);
	}	
	
	public Map<Integer, Double> getRankedTagList(int userID, int resID) {
		Map<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();

		// TODO: calculate your recommendations here and return the top-10 (=REC_LIMIT) tags with probability value
		// have also a look on the other calculator classes!
		
		// TODO: in order to improve your content-based recommender, you can merge your results with other approaches like the ones from the LanguageModelCalculator or ActCalculator
		
		return resultMap;
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public static List<Map<Integer, Double>> startContentBasedCreation(BookmarkReader reader, int sampleSize) {
		int size = reader.getBookmarks().size();
		int trainSize = size - sampleSize;
		
		ContentBasedCalculator calculator = new ContentBasedCalculator(reader, trainSize);
		List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();
		if (trainSize == size) {
			trainSize = 0;
		}
		
		for (int i = trainSize; i < size; i++) { // the test-set
			Bookmark data = reader.getBookmarks().get(i);
			Map<Integer, Double> map = calculator.getRankedTagList(data.getUserID(), data.getWikiID());
			results.add(map);
		}
		return results;
	}
	
	public static BookmarkReader predictSample(String filename, int trainSize, int sampleSize) {
		BookmarkReader reader = new BookmarkReader(trainSize, false);
		reader.readFile(filename);

		List<Map<Integer, Double>> modelValues = startContentBasedCreation(reader, sampleSize);
		
		List<int[]> predictionValues = new ArrayList<int[]>();
		for (int i = 0; i < modelValues.size(); i++) {
			Map<Integer, Double> modelVal = modelValues.get(i);
			predictionValues.add(Ints.toArray(modelVal.keySet()));
		}
		String suffix = "_cb";
		reader.setUserLines(reader.getBookmarks().subList(trainSize, reader.getBookmarks().size()));
		PredictionFileWriter writer = new PredictionFileWriter(reader, predictionValues);
		String outputFile = filename + suffix;
		writer.writeFile(outputFile);
		
		return reader;
	}
}
