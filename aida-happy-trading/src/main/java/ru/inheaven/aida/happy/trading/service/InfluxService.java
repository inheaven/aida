package ru.inheaven.aida.happy.trading.service;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
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
        connect();

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
            try {
                ping.set(influxDB.ping() != null);
            } catch (Exception e) {
                ping.set(false);

                connect();

                log.error("error influx db ping", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void connect(){
        try {
            influxDB = InfluxDBFactory.connect("http://corets.ru:8086", "corets", "corets");
            influxDB.enableBatch(100, 1, TimeUnit.SECONDS);

            ping.set(true);
        } catch (Exception e) {
            log.error("error influx bd connect", e);
        }
    }

    public void addAccountMetric(Long accountId, BigDecimal price, BigDecimal free, BigDecimal subtotal, BigDecimal total, BigDecimal net){
        if (ping.get()) {
            influxDB.write(DB_NAME, RETENTION_POLICY, Point.measurement("account")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("accountId", accountId.toString())
                    .addField("price", price)
                    .addField("free", free)
                    .addField("subtotal", subtotal)
                    .addField("total", total)
                    .addField("net", net)
                    .build());
        }
    }

    public void addStrategyMetric(Long strategyId, BigDecimal lot, BigDecimal spread, BigDecimal stdDev, Double balance,
                                  Double forecast, BigDecimal shift){
        if (ping.get()){
            influxDB.write(DB_NAME, RETENTION_POLICY, Point.measurement("strategy")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("strategyId", strategyId.toString())
                    .addField("lot", lot)
                    .addField("spread", spread)
                    .addField("std_dev", stdDev)
                    .addField("spot_balance", balance)
                    .addField("forecast", forecast)
                    .addField("shift", shift)
                    .build());
        }
    }


}
