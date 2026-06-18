//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import picocli.CommandLine;
//import utils.Configuration;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.ExecutionException;
//import java.util.stream.Stream;
//import java.util.zip.GZIPInputStream;
//
//
//public class SendGzipToKafka_noGuava {
//
//    private static long longKey = 0;
//    private static ObjectMapper objectMapper = new ObjectMapper();
//
//    @CommandLine.Option(names = "-D")
//    void setProperty(Map<String, String> props) {
//        props.forEach((k, v) -> System.setProperty(k, v == null ? "" : v));
//    }
//
//    public static void main(final String[] args) throws IOException {
//        Configuration params = new Configuration(args);
//        String topic = params.topic;
//        String bootStrapServers = params.kafkaBootStrapServers;
//        String gzipFile = params.gzipFile;
//        Integer rateLimiter = (int) (params.rateLimiter * 1000);
//        System.out.println(params);
//
//        Properties properties = new Properties();
//        // Set the brokers (bootstrap servers)
//        properties.setProperty("bootstrap.servers", bootStrapServers);
//        // Set how to serialize key/value pairs
//        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
//
//        long lineCount = countGzipJsonLines(gzipFile);
//        Stream<String> lines = readGzipJsonLines(gzipFile);
//        long startTime = System.currentTimeMillis();
//        streamJsonlGzipToKafka(gzipFile, producer, topic, rateLimiter, lines);
//        producer.flush();
//
//
//        long endTime = System.currentTimeMillis();
//        long duration = (endTime - startTime) / 1000;
//        long measuredRate = lineCount / duration;
//
//        System.out.println("Sent " + lineCount + " lines in... " + duration + " seconds");
//        System.out.println("Measured Input Rate: " + measuredRate + " lines/seconds");
//        producer.close();
//    }
//
//    public static void streamJsonlGzipToKafka(
//            String gzipFile,
//            KafkaProducer<String, String> producer,
//            String topic, Integer delay, Stream<String> lines) {
//
//
//        System.out.println("Writing to Kafka Topics47589346: " + topic + "... ... . ");
//
//        lines.forEach(jsonLine -> {
//            try {
//                sendTuple(producer, topic, jsonLine);
////                    if (delay != 0)
////                        Thread.sleep(delay);
//
//            } catch (IOException | ExecutionException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//
//    }
//
//    public static Stream<String> readGzipJsonLines(String filePath) throws IOException {
//        InputStream fileStream = new FileInputStream(filePath);
//        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
//        InputStreamReader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
//        BufferedReader buffered = new BufferedReader(decoder);
//
//        return buffered.lines(); // Java Stream<String> of JSON lines
//    }
//
//    public static long countGzipJsonLines(String filePath) throws IOException {
//        try (InputStream fileStream = new FileInputStream(filePath);
//             GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(gzipStream, StandardCharsets.UTF_8))) {
//            return reader.lines().count();
//        }
//    }
//
//
//    private static void sendTuple(
//            KafkaProducer<String, String> producer,
//            String topic,
//            String jsonLine) throws ExecutionException, InterruptedException, JsonProcessingException {
//
////        JsonNode json = objectMapper.readTree(jsonLine);
////        String value = parseString(json);
//
//        ProducerRecord<String, String> record = new ProducerRecord<>(
//                topic,
//                    String.valueOf(longKey++),
////                value,   // preserve ordering
//                jsonLine
//        );
//        producer.send(record);
//
//    }
//
//    private static String parseString(JsonNode jsonNode) {
//        JsonNode numFeatureListJson = jsonNode.get("numFeatureList");
//        String numFeatureList = "";
//        while (numFeatureListJson.elements().hasNext()) {
//            numFeatureList = numFeatureListJson.elements().next().toString();
//            break;
//        }
//        return numFeatureList;
//    }
//
//
//}