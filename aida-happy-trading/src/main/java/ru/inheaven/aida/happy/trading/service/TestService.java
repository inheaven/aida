package ru.inheaven.aida.happy.trading.service;

import javax.inject.Singleton;
import java.util.Date;

/**
 * @author inheaven on 16.06.2015 20:12.
 */
@Singleton
public class TestService {
    private int count = 0;

    public String getCurrentHelloWorld(){
        return "Hello World " + new Date().toString() + " " + count++;
    }
}
