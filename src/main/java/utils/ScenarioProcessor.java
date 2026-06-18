package utils;

import object.Node;
import object.Tuple2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ScenarioProcessor {

    public static String process(
            String nodeFilePath,
            String scenarioFilePath,
            String outputFile,
            String dateFormat) throws IOException {

        File outFile = new File(outputFile);

        GzipJsonlWriter.init(outFile.getAbsolutePath());

        Map<Integer, Node> nodeMap = loadNodes(nodeFilePath);

        long startTime = System.currentTimeMillis();

        try (BufferedReader br = new BufferedReader(new FileReader(scenarioFilePath))) {

            String line;
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] texts = line.split(",");

                // Edge record
                if (isNumber(texts[0].trim())) {

                    int pos = 0;
                    int edgeId = Integer.parseInt(texts[pos++].trim());
                    int startId = Integer.parseInt(texts[pos++].trim());
                    int endId = Integer.parseInt(texts[pos++].trim());

                    Tuple2<List<Double>, List<String>> features =
                            parseFeatures(texts, pos);

                    String timestamp = parseTimestamp(texts, sdf);

                    writeTuple(
                            edgeId,
                            nodeMap.get(startId),
                            nodeMap.get(endId),
                            features.f0,
                            features.f1,
                            timestamp,
                            dateFormat
                    );
                }
                // wait command
                else if ("wait".equals(texts[0].trim())) {
                    sleep(texts);
                }
            }
        }

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Completed in: " + duration + " seconds");

        GzipJsonlWriter.close();

        return outFile.getAbsolutePath();
    }

    // -------- Node Loading --------

    private static Map<Integer, Node> loadNodes(String path) throws IOException {

        Map<Integer, Node> map = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] texts = line.split(",");

                int pos = 0;
                int nodeId = Integer.parseInt(texts[pos++].trim());

                int blockId = -1;

                if (texts.length > pos && texts[pos].trim().startsWith("b-")) {
                    blockId = Integer.parseInt(
                            texts[pos++].trim().substring(2)
                    );
                }

                Tuple2<List<Double>, List<String>> features =
                        parseFeatures(texts, pos);

                map.put(
                        nodeId,
                        (blockId == -1)
                                ? new Node(nodeId, features.f0, features.f1)
                                : new Node(nodeId, blockId, features.f0, features.f1)
                );
            }
        }

        return map;
    }

    // -------- Feature Parsing --------

    private static Tuple2<List<Double>, List<String>> parseFeatures(
            String[] texts,
            int pos) {

        List<Double> nums = new ArrayList<>();
        List<String> textsList = new ArrayList<>();

        if (texts.length <= pos) {
            return new Tuple2<>(nums, textsList);
        }

        extractFeatures(texts[pos++], nums, textsList);

        if (texts.length > pos) {
            extractTextFeaturesOnly(texts[pos], textsList);
        }

        return new Tuple2<>(nums, textsList);
    }

    private static void extractFeatures(
            String raw,
            List<Double> nums,
            List<String> texts) {

        String[] parts = raw.trim().split("[\\x20\\t]+");

        if (parts.length == 0) {
            return;
        }

        if (isNumber(parts[0])) {
            for (String p : parts) {
                nums.add(Double.parseDouble(p.trim()));
            }
        } else {
            extractTextFeaturesOnly(raw, texts);
        }
    }

    private static void extractTextFeaturesOnly(
            String raw,
            List<String> texts) {

        String[] parts = raw.trim().split("[\\x20\\t]+");

        for (String p : parts) {

            String t = p.trim();

            if (t.length() >= 2 &&
                    t.startsWith("\"") &&
                    t.endsWith("\"")) {

                texts.add(t.substring(1, t.length() - 1));
            }
        }
    }

    // -------- Timestamp --------

    private static String parseTimestamp(
            String[] texts,
            SimpleDateFormat sdf) {

        try {
            String t = texts[texts.length - 1].trim();
            sdf.parse(t);
            return t;
        } catch (ParseException e) {

            try {
                String t = texts[texts.length - 2].trim();
                sdf.parse(t);
                return t;
            } catch (ParseException ignored) {
                return null;
            }
        }
    }

    // -------- Output --------

    private static void writeTuple(
            int edgeId,
            Node start,
            Node end,
            List<Double> numFeatures,
            List<String> textFeatures,
            String timestamp,
            String dateFormat) throws IOException {

        String json = JsonNodeUtil.createTuple(
                edgeId,
                start,
                end,
                numFeatures,
                textFeatures,
                timestamp,
                dateFormat
        ).toString();

        GzipJsonlWriter.writeLine(json);
    }

    // -------- Utilities --------

    private static void sleep(String[] texts) {

        try {
            Thread.sleep(Long.parseLong(texts[1].trim()));
        } catch (InterruptedException ignored) {
        }
    }

    private static boolean isNumber(String text) {
        return Pattern.compile("[+-]?\\d*(\\.\\d+)?")
                .matcher(text)
                .matches();
    }
}