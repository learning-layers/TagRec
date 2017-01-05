package processing.hashtag.baseline;

public class VectorUtil {
    
    /**
     * Vector Abs value.
     * @param vector
     * @return
     */
    public static double getVectorAbsValue(Vector vector) {
        double vectorAbsValue = 0;
        for (Integer hashtag : vector.getVector().keySet()) {
            vectorAbsValue += (vector.getVector().get(hashtag) * vector.getVector().get(hashtag));
        }
        if(vectorAbsValue != 0){
            vectorAbsValue = Math.sqrt(vectorAbsValue);
        }
        return vectorAbsValue;
    }

    /**
     * Vector dot product.
     * @param v1
     * @param v2
     * @return
     */
    public static double getVectorDotProduct(Vector v1, Vector v2) {
        double dotProduct = 0d;
        for (Integer key : v1.getVector().keySet()) {
            if (v2.getVector().containsKey(key)) {
                dotProduct += v1.getVector().get(key) * v2.getVector().get(key);
            }
        }
        return dotProduct;
    }
    
    public static void main(String[] args){
        Vector vector1 = new Vector();
        vector1.getVector().put(1, 3.0);
        vector1.getVector().put(2, 4.0);
        //System.out.println(" Vector abs values >> " + VectorUtil.getVectorAbsValue(vector1));
        Vector vector2 = new Vector();
        vector2.getVector().put(1, 5.0);
        vector2.getVector().put(2, 4.0);
        //System.out.println(" Cosine sim >> " + CosineSimilarityCalculator.getCosineSimilarity(vector1, vector2));
    }
}
