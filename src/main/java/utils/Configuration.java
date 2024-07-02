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
    public String nodeFilePath;
    public String scenarioFilePath;

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
        dateFormat = kafka.getString("dateFormat");
        nodeFilePath = kafka.getString("nodeFilePath");
        scenarioFilePath = kafka.getString("scenarioFilePath");
    }

    private List<Double> getDoubleList(Config conf, String key) {
        List<Double> result = null;
        if(conf.getValue(key).valueType() == ConfigValueType.LIST) {
            result = conf.getDoubleList(key);
        } else  if (conf.getValue(key).valueType() == ConfigValueType.STRING){
            Config c = ConfigFactory.parseString("root:" + conf.getString(key));
            result = c.getDoubleList("root");
        }
        return result;
    }

    private List<Integer> getIntList(Config conf, String key) {
        List<Integer> result = null;
        if(conf.getValue(key).valueType() == ConfigValueType.LIST) {
            result = conf.getIntList(key);
        } else  if (conf.getValue(key).valueType() == ConfigValueType.STRING){
            Config c = ConfigFactory.parseString("root:" + conf.getString(key));
            result = c.getIntList("root");
        }
        return result;
    }

    private List<String> getStringList(Config conf, String key) {
        List<String> result = null;
        if(conf.getValue(key).valueType() == ConfigValueType.LIST) {
            result = conf.getStringList(key);
        } else  if (conf.getValue(key).valueType() == ConfigValueType.STRING){
            Config c = ConfigFactory.parseString("root:" + conf.getString(key));
            result = c.getStringList("root");
        }
        return result;
    }

    private String stringListToString(List<String> list) {
        StringBuffer buf = new StringBuffer();
        for (String str : list) {
            buf.append(str);
        }
        return buf.toString();
    }

    private ConfigList getConfigList(Config conf, String key) {
        ConfigList result = null;
        if(conf.getValue(key).valueType() == ConfigValueType.LIST) {
            result = conf.getList(key);
        } else  if (conf.getValue(key).valueType() == ConfigValueType.STRING){
            Config c = ConfigFactory.parseString("root:" + conf.getString(key));
            result = c.getList("root");
        }
        return result;
    }

    @Override
    public String toString() {
        return "topic = " + topic + ", " +
                "kafkaBootStrapServers = " + kafkaBootStrapServers + ", " +
                "dateFormat = " + dateFormat + ", " +
                "nodeFilePath = " + nodeFilePath + ", " +
                "scenarioFilePath = " + scenarioFilePath
                ;
    }
}
