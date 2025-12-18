import org.apache.kafka.clients.producer.*;
import picocli.CommandLine;
import utils.Configuration;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class SendGzipToKafka {

    private static long longKey = 0;

    @CommandLine.Option(names = "-D")
    void setProperty(Map<String, String> props) {
        props.forEach((k, v) -> System.setProperty(k, v == null ? "" : v));
    }

    public static void main(final String[] args) throws IOException {
        Configuration params = new Configuration(args);
        String topic = params.topic;
        String bootStrapServers = params.kafkaBootStrapServers;
        String gzipFile = params.gzipFile;
        System.out.println(params);

        Properties properties = new Properties();
        // Set the brokers (bootstrap servers)
        properties.setProperty("bootstrap.servers", bootStrapServers);
        // Set how to serialize key/value pairs
        properties.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        long lineCount = countGzipJsonLines(gzipFile);
        long startTime = System.currentTimeMillis();
        streamJsonlGzipToKafka(gzipFile, producer, topic);
        producer.flush();
        producer.close();

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime)/1000;  //3128710 lines
        long measuredRate = lineCount/duration;

        System.out.println("Sent " + lineCount + " lines in... " + duration + " seconds");
        System.out.println("Measured Input Rate: " + measuredRate + " lines/seconds");
    }

    public static void streamJsonlGzipToKafka(
            String gzipFile,
            KafkaProducer<String, String> producer,
            String topic) {

        System.out.println("Writing to Kafka Topic Async: " + topic + "...");

        try (Stream<String> lines = readGzipJsonLines(gzipFile)) {

            lines.forEach(jsonLine -> {
                try {
                    sendTuple(producer, topic, jsonLine);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Error reading gzip file: " + gzipFile, e);
        }
    }

    public static Stream<String> readGzipJsonLines(String filePath) throws IOException {
        InputStream fileStream = new FileInputStream(filePath);
        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
        InputStreamReader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
        BufferedReader buffered = new BufferedReader(decoder);

        return buffered.lines(); // Java Stream<String> of JSON lines
    }

    public static long countGzipJsonLines(String filePath) throws IOException {
        try (InputStream fileStream = new FileInputStream(filePath);
             GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzipStream, StandardCharsets.UTF_8))) {
            return reader.lines().count();
        }
    }


    private static void sendTuple(
            KafkaProducer<String, String> producer,
            String topic,
            String jsonLine) throws ExecutionException, InterruptedException {

        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                String.valueOf(longKey++),  // preserve ordering
                jsonLine
        );

        producer.send(record);   // blocks until acknowledged

    }

}