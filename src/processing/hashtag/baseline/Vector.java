package processing.hashtag.baseline;

import java.util.HashMap;

public class Vector {
    private HashMap<Integer, Double> vector;

    public Vector() {
        vector = new HashMap<Integer, Double>();
    }

    public HashMap<Integer, Double> getVector() {
        return vector;
    }

    public void setVector(HashMap<Integer, Double> vector) {
        this.vector = vector;
    }
}
