package src.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    String file = "config.properties";
    Properties prop = new Properties();
    private static PropertiesReader singleton;

    private PropertiesReader() {
        InputStream configFile = null;
        try {
            configFile = PropertiesReader.class.getClassLoader().getResourceAsStream(file);
            if(configFile == null){
                throw new RuntimeException("config file is missing!");
            }
            prop.load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(configFile!=null){
                try {
                    configFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized PropertiesReader getSingleton(){
        if(singleton == null){
            singleton = new PropertiesReader();
        }
        return singleton;
    }

    public String getProperty(String property) {
        return prop.getProperty(property);
    }
}
