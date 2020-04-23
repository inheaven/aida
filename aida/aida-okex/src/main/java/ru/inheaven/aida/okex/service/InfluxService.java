package ru.inheaven.aida.okex.service;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import javax.inject.Singleton;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Anatoly A. Ivanov
 * 07.01.2018 0:45
 */
@Singleton
public class InfluxService {
    private InfluxDB influxDB;

    public InfluxService() {
        influxDB = InfluxDBFactory
                .connect("http://localhost:8086", "aida", "aida")
                .enableBatch(1000, 1, SECONDS)
                .setDatabase("aida");
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }
}
