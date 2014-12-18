package userrecommender;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import Jama.Matrix;



/**
 * Implements the Spear Algorithm. This implementation is hidden therefore package
 * scope. Please use the appropriate wrapper class which transforms the given
 * data set into an appropriate input for this algorithm.
 * 
 * @author ilire.mavriqi
 *
 */
class SpearAlgorithm {

	private static final int DFEAULT_NUMBER_OF_ITERATIONS = 250;
	private Matrix matrix;
	private HashMap<Integer, TreeSet<UserActivity>> activities = null;

	private LinkedList<Integer> uniqueUsers = null;
	private List<Integer> uniqueResources = null;

	private Map<Integer, Double> expertiseResult = null;
	private Map<Integer, Double> qualityResult = null;

	/**
	 * creates a new Object.
	 * 
	 * @param activities
	 *            triples (resourceID, time stamp, userID). Presumption: the
	 *            given set is already sorted by time stamp whereby the
	 *            first entry is the earliest time stamp for the given resource.
	 * @param uniqueUsers
	 *            list of unique userIDs
	 * @param uniqueResources
	 *            list of unique resouceIDs
	 */
	SpearAlgorithm(HashMap<Integer, TreeSet<UserActivity>> activities,
			LinkedList<Integer> uniqueUsers, List<Integer> uniqueResources) {
		this.activities = activities;
		this.uniqueUsers = uniqueUsers;
		this.uniqueResources = uniqueResources;
	}

	/**
	 * Executes spear algorithm on the given data set by initializing the sparse
	 * matrix and calculating the credit scores.
	 * 
	 * @param numberOfIterations
	 *            number of iterations. Minimum number is 250.
	 */
	SpearAlgorithmResult execute(int numberOfIterations) {
		if (activities == null || activities.size() == 0
				|| uniqueUsers.size() == 0 || uniqueResources.size() == 0) {
			throw new IllegalArgumentException(
					"Invalid input data. Plase make sure that entries, unique users and unique resources do not equall zero");
		}

		int finalNumberOfIterations = numberOfIterations > DFEAULT_NUMBER_OF_ITERATIONS ? numberOfIterations
				: DFEAULT_NUMBER_OF_ITERATIONS;

		populateMatrix();

		applyCreditScores();

		generateScoreVectors(finalNumberOfIterations);
		
		SpearAlgorithmResult result = new SpearAlgorithmResult(expertiseResult,
				qualityResult);

		return result;
	}

	private void generateScoreVectors(int finalNumberOfIterations) {
		Matrix userExpertiseVector = getInitSparseVector(uniqueUsers.size());
		Matrix resourceQualityVector = getInitSparseVector ( uniqueResources.size());

		for (int i = 0; i < finalNumberOfIterations; i++) {
			
			userExpertiseVector = resourceQualityVector.times((matrix).transpose());
			resourceQualityVector = userExpertiseVector.times(matrix);
			userExpertiseVector.timesEquals(1. / getColumnSum (userExpertiseVector));   	// normalise
			resourceQualityVector.timesEquals(1. / getColumnSum(resourceQualityVector));  	// normalise
		}

		expertiseResult = new HashMap<Integer, Double>();
		qualityResult = new HashMap<Integer, Double>();

		for (int j = 0; j < userExpertiseVector.getColumnDimension(); j++) {
			expertiseResult.put( uniqueUsers.get(j),userExpertiseVector.get(0,j));
		}
		
		for (int j = 0; j < resourceQualityVector.getColumnDimension(); j++) {
			qualityResult.put(uniqueResources.get(j),resourceQualityVector.get(0,j));
		}
		
	}

	private Matrix getInitSparseVector(int size) {
		Matrix spVec = new Matrix(1, size);
		for (int i = 0; i < spVec.getColumnDimension(); i++) {
			spVec.set(0, i, 1d);
		}
		return spVec;
	}
	
	private double getColumnSum (Matrix matrix){
		double sum = 0;
		for(int j=0;j<matrix.getColumnDimension();j++) {
			sum += matrix.get(0, j);
		}
		return sum;
	}

	private void applyCreditScores() {
		
		for (int i = 0; i < matrix.getRowDimension(); i++) {
			for (int j = 0; j < matrix.getColumnDimension(); j++) {
				double creditScore = matrix.get(i, j);
					matrix.set(i, j, Math.sqrt(creditScore));
			}
		}
	}

	private void populateMatrix() {
		matrix = new Matrix(uniqueUsers.size(),
				uniqueResources.size());

		Iterator<Integer> resourceIterator = activities.keySet().iterator();

		while (resourceIterator.hasNext()) {

			long lastTimeStamp = 0;
			double lastScore = 0d;
			int currentProcessedUsers = 0;

			Integer resourceID = resourceIterator.next();
			TreeSet<UserActivity> timeUserTuple = activities.get(resourceID);

			for (UserActivity activity : timeUserTuple) {
				if (activity.getTimeStamp() == 0) {
					throw new IllegalArgumentException(
							"Invalid time stamp found for resource with id: "
									+ resourceID + " and user id:"
									+ activity.getUserId());
				}
				if (activity.getTimeStamp() == lastTimeStamp) {
					matrix.set(uniqueUsers.indexOf(activity.getUserId()),
							uniqueResources.indexOf(resourceID), lastScore);

				} else {
					lastScore =   activities.get(resourceID).size() - currentProcessedUsers;
					lastTimeStamp = activity.getTimeStamp();
					matrix.set(uniqueUsers.indexOf(activity.getUserId()),
							uniqueResources.indexOf(resourceID), lastScore);
				}
				currentProcessedUsers += 1;
			}

		}
	}

}
