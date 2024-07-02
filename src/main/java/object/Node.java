package object;

import java.util.List;

public class Node {
    public int nodeId;
    public int blockId = -1;
    public List<Double> numFeatureList;
    public List<String> textFeatureList;

    public Node(int nodeId, List<Double> numFeatureList, List<String> textFeatureList) {
        this.nodeId = nodeId;
        this.numFeatureList = numFeatureList;
        this.textFeatureList = textFeatureList;
    }

    public Node(int nodeId, int blockId, List<Double> numFeatureList, List<String> textFeatureList) {
        this.nodeId = nodeId;
        this.blockId = blockId;
        this.numFeatureList = numFeatureList;
        this.textFeatureList = textFeatureList;
    }
}
