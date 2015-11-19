package userrecommender;

import java.util.Map;
/**
 * SpearAlgorithmResult stores two score vectors with user and recourse end scores
 * 
 * @author ilire.mavriqi
 *
 */

public class SpearAlgorithmResult {

	
	private Map<Integer, Double> expertiseResult = null;
	private Map<Integer, Double> qualityResult = null;
	
	public SpearAlgorithmResult() {
		
	}

	public SpearAlgorithmResult(Map<Integer, Double> expertiseResult,
			Map<Integer, Double> qualityResult) {
		this.expertiseResult = expertiseResult;
		this.qualityResult = qualityResult;
	}
	public Map<Integer, Double> getExpertiseResult() {
		return expertiseResult;
	}

	public void setExpertiseResult(Map<Integer, Double> expertiseResult) {
		this.expertiseResult = expertiseResult;
	}

	public Map<Integer, Double> getQualityResult() {
		return  qualityResult;
	}

	public void setQualityResult(Map<Integer, Double> qualityResult) {
		this.qualityResult = qualityResult;
	}
}
