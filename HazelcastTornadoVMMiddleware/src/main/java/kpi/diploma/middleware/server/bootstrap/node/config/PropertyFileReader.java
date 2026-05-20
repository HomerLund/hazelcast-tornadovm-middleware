package kpi.diploma.middleware.server.bootstrap.node.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyFileReader {
    public static Properties read(String filePath){
        Properties properties = new Properties();

        try(InputStream stream = new FileInputStream(filePath)){
            properties.load(stream);
            System.out.println("[Config] Successfully loaded properties from: " + filePath);
        }
        catch (IOException e){
            System.err.println("[Config] Could not find or read file: " + filePath);
            System.err.println("[Config] Falling back to default framework settings");
        }

        return properties;
    }
}
