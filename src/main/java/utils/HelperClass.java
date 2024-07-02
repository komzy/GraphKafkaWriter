package utils;

import java.util.ArrayList;
import java.util.List;

public class HelperClass {
    public static List<Double> cloneList(List<Double> list) {
        List<Double> ret = new ArrayList<>();
        for (Double d : list) {
            ret.add(d);
        }
        return ret;
    }
}
