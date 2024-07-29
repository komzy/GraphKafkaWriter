import object.Node;
import org.apache.kafka.clients.producer.*;
import picocli.CommandLine;
import utils.Configuration;
import utils.JsonNodeUtil;
import utils.Tuple2;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class SendToFlinkPregelMain {

    @CommandLine.Option(names = "-D")
    void setProperty(Map<String, String> props) {
        props.forEach((k, v) -> System.setProperty(k, v == null ? "" : v));
    }

    public static void main(final String[] args) throws IOException {
        Configuration params = new Configuration(args);
        String topic = params.topic;
        String bootStrapServers = params.kafkaBootStrapServers;
        String dateFormat = params.dateFormat;
        String nodeFilePath = params.nodeFilePath;
        String scenarioFilePath = params.scenarioFilePath;
        System.out.println(params);
        Map<Integer, Node> mapAllNode = getNodeListFromNodeFile(nodeFilePath);

        Properties properties = new Properties();
        // Set the brokers (bootstrap servers)
        properties.setProperty("bootstrap.servers", bootStrapServers);
        // Set how to serialize key/value pairs
        properties.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        File file = new File(scenarioFilePath);
        try (FileReader fr = new FileReader(file)) {
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                // 1行抽出
                while ((line = br.readLine()) != null) {
                    // 空行はスキップ
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    // カンマ区切り
                    String[] texts = line.split(",");
                    // タプル処理
                    if (isNumber(texts[0].trim())) {
                        int pos = 0;
                        int edgeId = Integer.parseInt(texts[pos++].trim());
                        int startNodeId = Integer.parseInt(texts[pos++].trim());
                        int endNodeId = Integer.parseInt(texts[pos++].trim());
                        // numFeatureListとtextFeatureList
                        Tuple2<List<Double>, List<String>> tuple2 = getNumFeatureListTextFeatureList(texts, pos);
                        // タイムスタンプ
                        String timestamp = null;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
                        try {
                            String textTime = texts[texts.length-1].trim();
                            long time = simpleDateFormat.parse(textTime).getTime();
                            timestamp = textTime;
                        }
                        catch (ParseException e) {
                            try {
                                String textTime = texts[texts.length-2].trim();
                                long time = simpleDateFormat.parse(textTime).getTime();
                                timestamp = textTime;
                            }
                            catch (ParseException e2) {
                                // タイムスタンプ指定なし
                            }
                        }
                        sendTuple(producer, topic, edgeId, mapAllNode.get(startNodeId),
                                mapAllNode.get(endNodeId), tuple2.f0, tuple2.f1, timestamp, dateFormat);
                    }
                    // 制御文
                    else {
                        // wait文
                        if (texts[0].trim().equals("wait")) {
                            processControlWait(texts);
                        }
                        // それ以外
                        else {
                            // 無視
                        }
                    }
                }
            }
        }
//        sendTextFile(producer, topic, filePathCites);
    }

    /**
     * 動作確認用。
     * 指定ファイルを1行ずつKafkaに送信する。
     *
     * @param producer producerインスタンス
     * @param topic Topic名
     * @param path　ファイルのパス
     * @throws IOException ファイル読み込み失敗
     */
    static void sendTextFile(KafkaProducer<String, String> producer, String topic, String path) throws IOException {
        File file = new File(path);
        try (FileReader fr = new FileReader(file)) {
            try (BufferedReader br = new BufferedReader(fr)) {
                String text;
                while ((text = br.readLine()) != null) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, text);
                    producer.send(record);
                    producer.flush();
                }
            }
        }
        producer.close();
    }

    /**
     * Nodeファイルを読み出しMapを作成する。
     *
     * @param path　Nodeファイルのパス
     * @return ノードクラスのMap
     * @throws IOException ファイル読み込み失敗
     */
    private static Map<Integer, Node> getNodeListFromNodeFile(String path) throws IOException {
        Map<Integer, Node> map = new HashMap<>();
        File file = new File(path);
        try (FileReader fr = new FileReader(file)) {
            try (BufferedReader br = new BufferedReader(fr)) {
                String text;
                // 1行抽出
                while ((text = br.readLine()) != null) {
                    int nodeId;
                    int blockId = -1;
                    int pos = 0;
                    // カンマ区切り
                    String[] texts = text.split(",");
                    // ノードID
                    nodeId = Integer.parseInt(texts[pos++].trim());

                    if (nodeId == 174425) {
                        System.out.println(nodeId);
                    }

                    // ブロックID
                    if (texts[pos].trim().startsWith("b-")) {
                        blockId = Integer.parseInt(texts[pos++].trim().substring("b-".length()));
                    }
                    // numFeatureListとtextFeatureList
                    Tuple2<List<Double>, List<String>> tuple2 = getNumFeatureListTextFeatureList(texts, pos);
                    map.put(nodeId, getNode(nodeId, blockId, tuple2.f0, tuple2.f1));
                }
            }
        }
        return map;
    }

    /**
     * numFeatureListとtextFeatureListを取得する。
     *
     * @param texts 対象文字列配列
     * @param pos 対象文字列配列の検索開始位置（index）
     * @return numFeatureListとtextFeatureListのTuple2
     */
    private static Tuple2<List<Double>, List<String>> getNumFeatureListTextFeatureList(String[] texts, int pos) {
        List<Double> numFeatureList = new ArrayList<>();
        List<String> textFeatureList = new ArrayList<>();

        // 以降の情報なし
        if (texts.length <= pos) {
            return new Tuple2<>(numFeatureList, textFeatureList);
        }
        // numFeatureList
        String[] featureList = texts[pos++].trim().split("[\\x20\\t]+");
        if (featureList.length > 0) {
            // 数値
            if (isNumber(featureList[0])) {
                for (String feature: featureList) {
                    numFeatureList.add(Double.parseDouble(feature.trim()));
                }
            }
            // 文字列
            else {
                for (String feature: featureList) {
                    // "" で囲まれている
                    if (feature.trim().charAt(0) == '\"' && feature.trim().charAt(feature.length()-1) == '\"') {
                        textFeatureList.add(feature.trim().replace("\"", ""));
                    }
                }
            }
        }
        // 以降の情報なし
        if (texts.length <= pos) {
            return new Tuple2<>(numFeatureList, textFeatureList);
        }
        featureList = texts[pos].trim().split("[\\x20\\t]+");
        // 文字列
        if (featureList.length > 0) {
            for (String feature: featureList) {
                // "" で囲まれている
                if (feature.trim().charAt(0) == '\"' && feature.trim().charAt(feature.length()-1) == '\"') {
                    textFeatureList.add(feature.trim().replace("\"", ""));
                }
            }
        }
        return new Tuple2<>(numFeatureList, textFeatureList);
    }

    /**
     * Nodeインスタンスを生成し返す。
     *
     * @param nodeId ノードID
     * @param blockId ブロックID
     * @param numFeatureList numFeatureList
     * @param textFeatureList textFeatureList
     * @return Nodeインスタンス
     */
    private static Node getNode(int nodeId, int blockId, List<Double> numFeatureList, List<String> textFeatureList) {
        // ブロックIDなし
        if (blockId == -1) {
            return new Node(nodeId, numFeatureList, textFeatureList);
        }
        // ブロックIDあり
        else {
            return new Node(nodeId, blockId, numFeatureList, textFeatureList);
        }
    }


    /**
     * wait処理。<br>
     * 指定時間waitする。
     *
     * @param texts　シナリオ1行分
     */
    private static void processControlWait(String[] texts) {
        long millis = Integer.parseInt(texts[1].trim());
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * タプル送信。
     *
     * @param producer Producerインスタンス
     * @param topic トピック名
     * @param edgeId　エッジID
     * @param startNode 始点ノード
     * @param endNode 終点ノード
     * @param numFeatureList エッジのnumFeatureList
     * @param textFeatureList エッジのtextFeatureList
     * @param timestamp タイムスタンプ
     * @param dateFormat タイムスタンプのフォーマット
     */
    private static void sendTuple(
            KafkaProducer<String, String> producer, String topic, int edgeId, Node startNode, Node endNode,
            List<Double> numFeatureList, List<String> textFeatureList, String timestamp, String dateFormat) {
        // タプル送信
        ProducerRecord<String, String> record
                = new ProducerRecord<>(
                        topic, JsonNodeUtil.createTuple(edgeId, startNode, endNode, numFeatureList, textFeatureList,
                        timestamp, dateFormat).toString());
        producer.send(record);
        producer.flush();
    }

    /**
     * 数値かどうか判定する
     *
     * @param text 判定対象文字列
     * @return 数値の場合はtrue。そうでない場合はfalse。
     */
    private static boolean isNumber(String text) {
//        Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
        Pattern pattern = Pattern.compile("[+-]?\\d*(\\.\\d+)?");
        return pattern.matcher(text).matches();
    }
}