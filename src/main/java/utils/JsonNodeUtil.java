package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import object.Node;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class JsonNodeUtil {
     /**
     *　タプル生成。
     *
     * @param edgeId　エッジID
     * @param startNode 始点ノード
     * @param endNode 終点ノード
     * @param numFeatureList エッジのnumFeatureList
     * @param textFeatureList エッジのtextFeatureList
     * @param timestamp タイムスタンプ
     * @param dateFormat タイムスタンプのフォーマット
     * @return 1タプルのJsonNodeオブジェクト
     */
    public static JsonNode createTuple(
            int edgeId, Node startNode, Node endNode, List<Double> numFeatureList, List<String> textFeatureList,
            String timestamp, String dateFormat) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        root.put("edgeId", edgeId);

        ObjectNode nodeStartNode = mapper.createObjectNode();
        nodeStartNode.put("nodeId", startNode.nodeId);
        if (startNode.blockId >= 0) {
            nodeStartNode.put("blockId", startNode.blockId);
        }
        ArrayNode arrStartNodeNumFeatureList = nodeStartNode.putArray("numFeatureList");
        for (Double d : startNode.numFeatureList) {
            arrStartNodeNumFeatureList.add(d);
        }
        ArrayNode arrStartNodeTextFeatureList = nodeStartNode.putArray("textFeatureList");
        for (String s : startNode.textFeatureList) {
            arrStartNodeTextFeatureList.add(s);
        }
        root.set("startNode", nodeStartNode);

        ObjectNode nodeEndNode = mapper.createObjectNode();
        nodeEndNode.put("nodeId", endNode.nodeId);
        if (endNode.blockId >= 0) {
            nodeEndNode.put("blockId", endNode.blockId);
        }
        ArrayNode arrEndNodeNumFeatureList = nodeEndNode.putArray("numFeatureList");
        for (Double d : endNode.numFeatureList) {
            arrEndNodeNumFeatureList.add(d);
        }
        ArrayNode arrEndNodeTextFeatureList = nodeEndNode.putArray("textFeatureList");
        for (String s : endNode.textFeatureList) {
            arrEndNodeTextFeatureList.add(s);
        }
        root.set("endNode", nodeEndNode);

        ArrayNode arrNumFeatureList = root.putArray("numFeatureList");
        for (Double d : numFeatureList) {
            arrNumFeatureList.add(d);
        }

        ArrayNode arrTextList = root.putArray("textFeatureList");
        for (String s : textFeatureList) {
            arrTextList.add(s);
        }

        if (timestamp == null) {
            root.put("time", getTimeStamp(dateFormat));
        }
        else {
            root.put("time", timestamp);
        }
        return root;
    }

    /**
     *　タイムスタンプ文字列生成処理
     *
     * @param dateFormat タイムスタンプのフォーマット
     * @return タイムスタンプ文字列
     */
    public static String getTimeStamp(String dateFormat) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(timestamp);
    }
}