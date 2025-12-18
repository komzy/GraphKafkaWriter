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
    public String dateFormat;
    public String gzipFile;

    private Config conf;

    public Configuration(String... args) {
        this("kafka.conf", args);
    }

    public Configuration(String path, String... args) {
        if(args != null)
            new CommandLine(this).parseArgs(args);

        this.conf = ConfigFactory.load(path);
        conf.checkValid(ConfigFactory.defaultReference());
        this.setValues();
    }

    public void setValues(){
        Config kafka = conf.getConfig("kafka");

        topic = kafka.getString("topic");
        kafkaBootStrapServers = kafka.getString("kafkaBootStrapServers");
        gzipFile = kafka.getString("gzipFile");
    }


    @Override
    public String toString() {
        return "topic = " + topic + ", " +
                "kafkaBootStrapServers = " + kafkaBootStrapServers + ", " +
                "dateFormat = " + dateFormat + ", " +
                "gzipFile = " + gzipFile
                ;
    }
}
