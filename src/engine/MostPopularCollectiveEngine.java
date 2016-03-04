package engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import file.BookmarkReader;

public class MostPopularCollectiveEngine implements EngineInterface {

	private BookmarkReader reader;
	private final Map<String, Double> collectiveTags = new LinkedHashMap<String, Double>();
	
	public void loadFile(String path, String filename) throws Exception {
		BookmarkReader reader = EngineUtils.getSortedBookmarkReader(path, filename);
		Map<Integer, Double> collectiveTags = EngineUtils.calcTopEntities(reader, EntityType.TAG);
		
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
		List<Integer> filterTags = EngineUtils.getFilterTags(filterOwnEntities, this.reader, user, resource);
		
		Map<String, Double> returnMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : collectiveTags.entrySet()) {
			if (returnMap.size() < count.intValue()) {
				if (!filterTags.contains(entry.getKey())) {
					returnMap.put(entry.getKey(), entry.getValue());
				}
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
