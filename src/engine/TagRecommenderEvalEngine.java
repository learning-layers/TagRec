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

package engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import file.BookmarkReader;

public class TagRecommenderEvalEngine implements EngineInterface {

	//private EngineInterface mpEngine;
	private EngineInterface bllEngine;
	//private EngineInterface threelEngine;	
	//private Random random;
	private BufferedWriter bw;
	
	public TagRecommenderEvalEngine() {
		//this.mpEngine = null;
		this.bllEngine = null;
		//this.threelEngine = null;
		//this.random = new Random();
		this.bw = null;
	}
	
	@Override
	public void loadFile(String path, String filename) throws Exception {
		//this.lmEngine = null;
		//this.bllEngine = null;
		//this.threelEngine = null;
		
		//BookmarkReader reader = new BookmarkReader(0, false);
		//reader.readFile(filename);
		//if (reader.getCategories().size() > 0) {
		//	this.threelEngine = new ThreeLayersEngine();
		//	this.threelEngine.loadFile(filename);
		//}
		//if (reader.hasTimestamp()) {
			this.bllEngine = new BaseLevelLearningEngine();
			this.bllEngine.loadFile(path, filename);
		//}
		//this.lmEngine = new LanguageModelEngine();
		//this.lmEngine.loadFile(filename);
		
		/* KnowBrain
		//if (filename.contains("group1") || filename.contains("group3")) {
			this.threelEngine = new ThreeLayersCollectiveEngine();
			this.threelEngine.loadFile(filename);
		//} else {
			this.bllEngine = new BaseLevelLearningCollectiveEngine();
			this.bllEngine.loadFile(filename);
		//}
			this.mpEngine = new MostPopularCollectiveEngine();
			this.mpEngine.loadFile(filename);
		*/
			
		try {
			String logFile = "";
			if (path == null) {
				logFile = "./data/tagrec_log.txt";
			} else {
				logFile = path + "tagrec_log.txt";
			}
			FileWriter writer = new FileWriter(new File(logFile), true);
			this.bw = new BufferedWriter(writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized Map<String, Double> getEntitiesWithLikelihood(String user, String resource, List<String> topics, Integer count, Boolean filterOwnEntities, Algorithm algorithm, EntityType type) {
		Map<String, Double> returnMap = null;
		String algorithmString = null;
		

		if (this.bllEngine != null) {
			if (algorithm == null || algorithm == Algorithm.BLLacMPr) {
				algorithmString = "BLLacMPr";
			} else if (algorithm == Algorithm.BLLac) {
				algorithmString = "BLLac";
			} else if (algorithm == Algorithm.BLL) {
				algorithmString = "BLL";
			}
			if (algorithmString != null) {
				returnMap = this.bllEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
			}
		}		
		//if (algorithmString == null) {
		//	algorithmString = "MPur";
		//	returnMap = this.lmEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
		//}

		/* KnowBrain study
		if (algorithm == null || algorithm == Algorithm.THREELcoll || algorithm == Algorithm.THREEL) {
			if (this.threelEngine != null) {
				returnMap = this.threelEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
				if (algorithm == Algorithm.THREEL) {
					algorithmString = "3L";
				} else {
					algorithmString = "3Lcoll";
				}
			}
		} else if (algorithm == Algorithm.BLLcoll || algorithm == Algorithm.BLL) {
			if (this.bllEngine != null) {
				returnMap = this.bllEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
				if (algorithm == Algorithm.BLL) {
					algorithmString = "BLL";
				} else {
					algorithmString = "BLLcoll";
				}
			}
		} else {
			if (this.mpEngine != null) {
				returnMap = this.mpEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
				algorithmString = "MP";
			}
		}
		*/
		
		if (this.bw != null) {
			try {
				this.bw.write(user + "|" + resource + "|" + topics + "|" + count + "|" + filterOwnEntities + "|" + System.currentTimeMillis() + "|" + algorithmString + "|" + returnMap.keySet() + "\n");
				this.bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	   
	public static boolean getRandomBoolean() {
	       return Math.random() < 0.5;
	}
}
