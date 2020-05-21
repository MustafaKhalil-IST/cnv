package src.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    String file = "config.properties";
    Properties prop = new Properties();
    private static PropertiesReader instance;

    private PropertiesReader() {
        InputStream configfile = null;
        try {
            configfile = PropertiesReader.class.getClassLoader().getResourceAsStream(file);
            if(configfile == null){
                throw new RuntimeException("config file is missing");
            }
            prop.load(configfile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(configfile!=null){
                try {
                    configfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized PropertiesReader getInstance(){
        if(instance == null){
            instance = new PropertiesReader();
        }
        return instance;
    }

    public String getProperty(String property) {
        return prop.getProperty(property);
    }
}
