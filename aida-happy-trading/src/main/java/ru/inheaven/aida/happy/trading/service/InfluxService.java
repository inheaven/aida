package ru.inheaven.aida.happy.trading.service;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author inheaven on 01.10.2016.
 */
@Singleton
public class InfluxService {
    private final static String DB_NAME = "corets";
    private final static String RETENTION_POLICY = "autogen";

    private Logger log = LoggerFactory.getLogger(InfluxService.class);

    private InfluxDB influxDB;
    private AtomicBoolean ping = new AtomicBoolean(false);

    public InfluxService() {
        influxDB = InfluxDBFactory.connect("http://corets.ru:8086", "corets", "corets");
        influxDB.enableBatch(100, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
            try {
                ping.set(influxDB.ping() != null);
            } catch (Exception e) {
                ping.set(false);

                log.error("error influx db ping", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    


}
