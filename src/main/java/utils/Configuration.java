package utils;

import com.typesafe.config.*;
import picocli.CommandLine;

import java.util.*;

@CommandLine.Command
public class Configuration {

    // to set system parameters
    @CommandLine.Option(names = "-D")
    void setProperty(Map<String, String> props) {
        props.forEach((k, v) -> System.setProperty(k, v == null ? "" : v));
    }

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

    private Config conf;

    public Configuration(String... args) {
        this("parameters.conf", args);
    }

    public Configuration(String path, String... args) {
        if(args != null)
            new CommandLine(this).parseArgs(args);

        this.conf = ConfigFactory.load(path);
        conf.checkValid(ConfigFactory.defaultReference());
        this.setValues();
    }

    public void setValues(){
        Config parameters = conf.getConfig("parameters");
        topic = parameters.getString("topic");
        kafkaBootStrapServers = parameters.getString("kafkaBootStrapServers");
        rateLimiter = parameters.getDouble("rateLimiter");
        dateFormat = parameters.getString("dateFormat");
        nodeFilePath = parameters.getString("nodeFilePath");
        scenarioFilePath = parameters.getString("scenarioFilePath");
        outputFileName = parameters.getString("outputFileName");
        kafkaWrite = parameters.getBoolean("kafkaWrite");
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
