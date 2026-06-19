import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import picocli.CommandLine;
import utils.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.google.common.util.concurrent.RateLimiter;
import utils.ScenarioProcessor;

public class Main {


    public static void main(final String[] args) throws IOException {

        Configuration params = new Configuration(args);

        String topic = params.topic;
        String bootStrapServers = params.kafkaBootStrapServers;

        // User-defined rate (messages per second)
        double userRate = params.rateLimiter;

        System.out.println(params);

        String gzipFile = ScenarioProcessor.process(params.nodeFilePath, params.scenarioFilePath,
                params.outputFileName + ".jsonl.gz", params.dateFormat);

        System.out.println("gzipFile file: " + gzipFile);

        if (!params.kafkaWrite) {
            System.out.println("kafka write is set to False");
            System.exit(0);
        }

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootStrapServers);
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        long lineCount = countGzipJsonLines(gzipFile);

        // Create Guava RateLimiter
        RateLimiter rateLimiter = RateLimiter.create(userRate);

        long startTime = System.currentTimeMillis();

        try (Stream<String> lines = readGzipJsonLines(gzipFile)) {
            streamJsonlGzipToKafka(producer, topic, lines, rateLimiter);
        }

        producer.flush();
        producer.close();

        long endTime = System.currentTimeMillis();
        long durationSeconds = (endTime - startTime) / 1000;

        long measuredRate = durationSeconds > 0
                ? lineCount / durationSeconds
                : lineCount;

        System.out.println("Sent " + lineCount + " lines in " + durationSeconds + " seconds");
        System.out.println("Measured Input Rate: " + measuredRate + " lines/second");
    }

    public static void streamJsonlGzipToKafka(
            KafkaProducer<String, String> producer,
            String topic,
            Stream<String> lines,
            RateLimiter rateLimiter) {

        System.out.println("Writing to Kafka Topic blocks: " + topic + "...");

        lines.forEach(jsonLine -> {
            try {
                rateLimiter.acquire();

                sendTuple(producer, topic, jsonLine);

            } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Stream<String> readGzipJsonLines(String filePath) throws IOException {
        InputStream fileStream = new FileInputStream(filePath);
        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
        InputStreamReader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
        BufferedReader buffered = new BufferedReader(decoder);

        return buffered.lines();
    }

    public static long countGzipJsonLines(String filePath) throws IOException {
        try (InputStream fileStream = new FileInputStream(filePath);
             GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(gzipStream, StandardCharsets.UTF_8))) {

            return reader.lines().count();
        }
    }

    private static void sendTuple(
            KafkaProducer<String, String> producer,
            String topic,
            String jsonLine)
            throws ExecutionException, InterruptedException, IOException {

//        JsonNode json = objectMapper.readTree(jsonLine);
//        String value = parseString2(json);

        String value = parseString(jsonLine);
        ProducerRecord<String, String> record =
                new ProducerRecord<>(topic, value, jsonLine);

        producer.send(record);
    }

    private static final JsonFactory FACTORY = new JsonFactory();

    private static String parseString(String jsonLine) throws IOException {
        try (JsonParser parser = FACTORY.createParser(jsonLine)) {

            int objectDepth = 0;

            while (parser.nextToken() != null) {

                JsonToken token = parser.currentToken();

                if (token == JsonToken.START_OBJECT) {
                    objectDepth++;
                } else if (token == JsonToken.END_OBJECT) {
                    objectDepth--;
                }

                // Only match root-level field
                if (objectDepth == 1
                        && token == JsonToken.FIELD_NAME
                        && "numFeatureList".equals(parser.getCurrentName())) {

                    parser.nextToken(); // move to START_ARRAY

                    if (parser.nextToken() != JsonToken.END_ARRAY) {
                        return parser.getValueAsString();
                    }
                }
            }
        }
        return "";
    }


}