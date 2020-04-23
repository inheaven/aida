package ru.inheaven.aida.okex.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Anatoly A. Ivanov
 * 22.09.2017 18:40
 */
@Singleton
public class PropertyService {
    private Properties properties;

    @Inject
    public void init() throws IOException {
        properties = new Properties();

        properties.load(getClass().getResourceAsStream("/aida-okex.properties"));
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }
}
