package engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import processing.BLLCalculator;
import file.BookmarkReader;

public class BaseLevelLearningCollectiveEngine implements EngineInterface {

	private BookmarkReader reader = null;
	private final Map<String, Double> collectiveTags = new LinkedHashMap<String, Double>();
	
	public void loadFile(String path, String filename) throws Exception {
		BookmarkReader reader = EngineUtils.getSortedBookmarkReader(path, filename);
		Map<Integer, Double> collectiveTags = BLLCalculator.getCollectiveArtifactMap(reader, reader.getBookmarks(), null, false, new ArrayList<Long>(), new ArrayList<Double>(), 0.5, true);
		
		// map to strings
		Map<String, Double> collectiveTagNames = new LinkedHashMap<String, Double>();
		for (Map.Entry<Integer, Double> tag : collectiveTags.entrySet()) {
			collectiveTagNames.put(reader.getTags().get(tag.getKey()), tag.getValue());
		}
		
		resetStructures(reader, collectiveTagNames);
	}

	@Override
	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities, Algorithm algorithm, EntityType type) {
		if (count == null || count.doubleValue() < 1) {
			count = 10;
		}
		Map<String, Double> tagMap = new LinkedHashMap<String, Double>();		
		if (algorithm == null || algorithm == Algorithm.BLLcoll) {
			tagMap = this.collectiveTags;
		} else {
			int userID = this.reader.getUsers().indexOf(user);
			if (user != null && userID != -1) {
				Map<Integer, Double> userTags = BLLCalculator.getSortedArtifactMapForUser(userID, this.reader, this.reader.getBookmarks(), null, false, new ArrayList<Long>(), new ArrayList<Double>(), 0.5, true);
				for (Map.Entry<Integer, Double> tag : userTags.entrySet()) {
					tagMap.put(this.reader.getTags().get(tag.getKey()), tag.getValue());
				}
			}			
		}
		
		Map<String, Double> returnMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : tagMap.entrySet()) {
			if (returnMap.size() < count.intValue()) {
				returnMap.put(entry.getKey(), entry.getValue());
			} else {
				break;
			}
		}
		return returnMap;
	}

	private synchronized void resetStructures(BookmarkReader reader, Map<String, Double> collectiveTags) {
		this.reader = reader;
		this.collectiveTags.clear();
		this.collectiveTags.putAll(collectiveTags);
	}
}
