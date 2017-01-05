package common;

import java.util.Comparator;
import java.util.Map;

public class DoubleMapComparatorGeneric<T> implements Comparator<T> {
    private Map<T, Double> map;
    
    public DoubleMapComparatorGeneric(Map<T, Double> map) {
        this.map = map;
    }

    @Override
    public int compare(T key1, T key2) {
        Double val1 = this.map.get(key1);
        Double val2 = this.map.get(key2);
        if (val1 != null && val2 != null) {
            return (val1 >= val2 ? - 1 : 1);
        }
        return 0;
    }
}
