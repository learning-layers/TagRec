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

	private EngineInterface lmEngine;
	private EngineInterface bllEngine;
	private EngineInterface threelEngine;
	private EngineInterface mpEngine;
	
	//private Random random;
	private BufferedWriter bw;
	
	public TagRecommenderEvalEngine() {
		this.lmEngine = null;
		this.bllEngine = null;
		this.threelEngine = null;
		this.mpEngine = null;
		
		//this.random = new Random();
		this.bw = null;
	}
	
	@Override
	public void loadFile(String path, String filename) throws Exception {
		this.lmEngine = new LanguageModelEngine();
		this.lmEngine.loadFile(path, filename);
		this.bllEngine = new BaseLevelLearningEngine();
		this.bllEngine.loadFile(path, filename);
		this.threelEngine = new ThreeLayersCollectiveEngine();
		this.threelEngine.loadFile(path, filename);
		this.mpEngine = new MostPopularCollectiveEngine();
		this.mpEngine.loadFile(path, filename);
			
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
		
		if (algorithm == Algorithm.BLLacMPr) {
			if (this.bllEngine != null) {
				algorithmString = Algorithm.BLLacMPr.name();
				returnMap = this.bllEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);
			}
		} else if (algorithm == Algorithm.MPur) {
			if (this.lmEngine != null) {
				algorithmString = Algorithm.MPur.name();
				returnMap = this.lmEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);				
			}
		} else if (algorithm == Algorithm.THREELcoll) {
			if (topics != null && topics.size() > 0) {
				if (this.threelEngine != null) {
					algorithmString = Algorithm.THREELcoll.name();
					returnMap = this.threelEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);				
				}
			} else {
				if (this.mpEngine != null) {
					algorithmString = Algorithm.THREELcoll.name();
					returnMap = this.mpEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);				
				}
			}
		} else {
			if (this.mpEngine != null) {
				algorithmString = Algorithm.MP.name();
				returnMap = this.mpEngine.getEntitiesWithLikelihood(user, resource, topics, count, filterOwnEntities, algorithm, type);				
			}
		}

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
