package engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import common.CalculationType;
import processing.BLLCalculator;
import processing.ThreeLTCalculator;
import file.BookmarkReader;

public class ThreeLayersCollectiveEngine implements EngineInterface {

	private BookmarkReader reader = null;
	private ThreeLTCalculator calculator = null;
	
	public void loadFile(String path, String filename) throws Exception {
		BookmarkReader reader = EngineUtils.getSortedBookmarkReader(path, filename);
		ThreeLTCalculator calculator = new ThreeLTCalculator(reader, reader.getBookmarks().size(), 5, 5, true, false, false, CalculationType.NONE);
		
		resetStructures(reader, calculator);
	}

	@Override
	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities, Algorithm algorithm, EntityType type) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		List<Integer> topicIDs = new ArrayList<>();
		if (topics != null) {
			for (String t : topics) {
				int tID = this.reader.getCategories().indexOf(t);
				if (tID != -1) {
					topicIDs.add(tID);
				}
			}
		}		
		Map<Integer, Double> tagIDs = null;
		if (algorithm == null || algorithm == Algorithm.THREELcoll) {
			tagIDs = this.calculator.getCollectiveRankedTagList(topicIDs, System.currentTimeMillis() / 1000.0, count.intValue(), false, false);
		} else {
			int userID = this.reader.getUsers().indexOf(user);
			if (user != null && userID != -1) {
				tagIDs = this.calculator.getRankedTagList(userID, -1, topicIDs, System.currentTimeMillis() / 1000.0, count.intValue(), false, false, true);
			} else {
				new LinkedHashMap<String, Double>();
			}
		}
		
		// map to strings
		Map<String, Double> tagStrings = new LinkedHashMap<String, Double>();
		for (Map.Entry<Integer, Double> entry : tagIDs.entrySet()) {
			tagStrings.put(this.reader.getTags().get(entry.getKey()), entry.getValue());
		}
		return tagStrings;
	}

	private synchronized void resetStructures(BookmarkReader reader, ThreeLTCalculator calculator) {
		this.reader = reader;
		this.calculator = calculator;
	}
}
