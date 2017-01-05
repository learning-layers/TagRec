package processing.hashtag.baseline;

import org.junit.Test;
import junit.framework.Assert;


/**
 * 
 * @author spujari
 *
 */
public class VectorUtilTest {
   
    @Test
    public void calculateVectorSimilarityTest(){
        Assert.assertEquals(true, true);
    }
    
    @Test
    public void calculateSimScoreTest(){
        Vector v1 = new Vector();
        Vector v2 = new Vector();
        double dotproduct = VectorUtil.getVectorDotProduct(v1, v2);
        System.out.println(" hello world >> " + dotproduct);
        
    }
}
