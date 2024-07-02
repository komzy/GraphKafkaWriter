package object;

import java.util.List;

/**
 * 送信データ（タプル）クラス。
 */
public class Tuple {
    public int edgeId;
    public int startNodeId;
    public int endNodeId;
    public List<Double> numFeatureList;
    public List<String> textFeatureList;
    public String timestamp;
}
