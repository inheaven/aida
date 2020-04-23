package ru.inheaven.aida.sensor;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 * 27.05.2018 10:58
 */
public class Influx {
    private static Influx influx;

    private InfluxDB influxDB;

    public Influx() {
        influxDB = InfluxDBFactory.connect("http://192.168.0.16:8086", "root", "root");
        influxDB.setDatabase("sensor");
        influxDB.enableBatch(10000, 1, TimeUnit.SECONDS);
    }

    public static Influx getInstance(){
        if (influx == null){
            influx = new Influx();
        }

        return influx;
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }
}
