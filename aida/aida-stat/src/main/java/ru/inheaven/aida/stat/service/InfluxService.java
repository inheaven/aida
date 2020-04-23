package ru.inheaven.aida.stat.service;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author inheaven on 20.12.2016.
 */
@Singleton
public class InfluxService {
    private final static String DB = "bitindex";
    private final static String RP = "autogen";

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
            influxDB = InfluxDBFactory.connect("http://45.115.36.126:8086", "bitindex", "bitindex");
            influxDB.enableBatch(100, 1, TimeUnit.SECONDS);

            ping.set(true);
        } catch (Exception e) {
            log.error("error influx bd connect", e);
        }
    }

    private boolean hasBlock(String hash){
        QueryResult result = influxDB.query(new Query("select * from \"block\" where \"hash\" = '" + hash + "'", DB));

        return result.getResults().isEmpty() || result.getResults().get(0).getSeries() != null;
    }

    public void addBlock(int height, Block block){
        if (ping.get()) {
//            if (!hasBlock(block.getHashAsString())) {
                List<Transaction> transactions = block.getTransactions();

                influxDB.write(DB, RP, Point.measurement("block")
                        .time(block.getTimeSeconds(), TimeUnit.SECONDS)
                        .tag("hash", block.getHashAsString())
                        .addField("height", height)
                        .addField("bytes", block.bitcoinSerialize().length)
                        .addField("transaction_count", transactions != null ? transactions.size() : 0)
                        .addField("transaction_output_sum", transactions != null ? transactions.stream().mapToLong(t -> t.getOutputSum().getValue()).sum() : 0)
                        .build());

                log.info("add block {}", height);
            }
//        }
    }
}
