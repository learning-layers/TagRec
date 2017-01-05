package processing.hashtag.baseline;

/**
 * @author spujari
 */
public class CosineSimilarityCalculator{
    
    /**
     * Get the cosine similarity for the vectors.
     * @param v1
     * @param v2
     * @return
     */
    public static double getCosineSimilarity(Vector v1, Vector v2) {
        double similarity = 0d;
        double absValueVector1 = VectorUtil.getVectorAbsValue(v1);
        double absValueVector2 = VectorUtil.getVectorAbsValue(v2);
        if (absValueVector1 != 0 && absValueVector2 != 0) {
            similarity = VectorUtil.getVectorDotProduct(v1, v2) / (absValueVector1 * absValueVector2);
        }
        return similarity;
    }
    
    
}


