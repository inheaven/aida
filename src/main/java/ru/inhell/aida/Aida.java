package ru.inhell.aida;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.04.11 17:45
 */
public class Aida {
    private static Properties properties = null;

    public static String getProperty(String key){
        if (properties == null){
            try {
                properties = new Properties();
                properties.load(ClassLoader.getSystemResourceAsStream("aida.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return properties.getProperty(key);
    }

    public static String getQuikDir(){
        return getProperty("quik_dir");
    }
}
