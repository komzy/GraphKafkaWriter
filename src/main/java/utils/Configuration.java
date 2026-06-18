package utils;

import com.typesafe.config.*;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

@CommandLine.Command
public class Configuration {

    /**
     * Parameters
     */
    public String topic;
    public String kafkaBootStrapServers;
    public Double rateLimiter;
    public String dateFormat;
    public String nodeFilePath;
    public String scenarioFilePath;
    public String outputFileName;
    public Boolean kafkaWrite;

    private Map<String, Object> config;

    public Configuration(String... args) throws FileNotFoundException {
        this("parameters.yml", args);
    }

    public Configuration(String path, String... args) throws FileNotFoundException {
        if(args != null) new CommandLine(this).parseArgs(args);

        loadYaml(path);
        setValues();
    }

    private void loadYaml(String yamlFile) throws FileNotFoundException {

        String YAML_PATH = new File(".").getAbsoluteFile().getParent() + File.separator +
                "conf" + File.separator + yamlFile;

        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(YAML_PATH);
        if (inputStream == null) {
            throw new RuntimeException("Cannot find config file: " + YAML_PATH);
        }
        config = yaml.load(inputStream);
    }

    public void setValues(){
        Map<String, Object> parameters = (Map<String, Object>) config.get("parameters");
        topic = (String) parameters.get("topic");
        kafkaBootStrapServers = (String) parameters.get("kafkaBootStrapServers");
        rateLimiter = (Double) parameters.get("rateLimiter");
        dateFormat = (String) parameters.get("dateFormat");
        nodeFilePath = (String) parameters.get("nodeFilePath");
        scenarioFilePath = (String) parameters.get("scenarioFilePath");
        outputFileName = (String) parameters.get("outputFileName");
        kafkaWrite = (Boolean) parameters.get("kafkaWrite");
    }


    @Override
    public String toString() {
        return "dateFormat = " + dateFormat + ", " +
                "nodeFilePath = " + nodeFilePath + ", " +
                "scenarioFilePath = " + scenarioFilePath + ", " +
                "outputFileName = " + outputFileName + ", "  +
                "kafkaWrite = " + kafkaWrite + ", "  +
                "topic = " + topic + ", " +
                "kafkaBootStrapServers = " + kafkaBootStrapServers + ", " +
                "rateLimiter = " + rateLimiter + " sec";
    }
}
