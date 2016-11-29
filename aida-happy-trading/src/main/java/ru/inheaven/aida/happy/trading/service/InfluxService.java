package ru.inheaven.aida.happy.trading.service;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;

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

    public void addAccountMetric(Long accountId, BigDecimal price, BigDecimal free, BigDecimal subtotal, BigDecimal total, BigDecimal net,
                                 BigDecimal openBid, BigDecimal openAsk){
        if (ping.get()) {
            influxDB.write(DB_NAME, RETENTION_POLICY, Point.measurement("account")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("accountId", accountId.toString())
                    .addField("price", price)
                    .addField("free", free)
                    .addField("subtotal", subtotal)
                    .addField("total", total)
                    .addField("net", net)
                    .addField("open_bid", openBid)
                    .addField("open_ask", openAsk)
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

    public void addOrderMetric(Long strategyId, BigDecimal openAskPrice, BigDecimal openAskVolume, Integer openAskCount,
                               BigDecimal openBidPrice, BigDecimal openBidVolume, Integer openBidCount,
                               BigDecimal closedAskPrice, BigDecimal closedAskVolume, Integer closedAskCount,
                               BigDecimal closedBidPrice, BigDecimal closedBidVolume, Integer closedBidCount){
        if (ping.get()){
            influxDB.write(DB_NAME, RETENTION_POLICY, Point.measurement("order")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("strategyId", strategyId.toString())
                    .addField("open_ask_price", openAskPrice)
                    .addField("open_ask_volume", openAskVolume)
                    .addField("open_ask_count", openAskCount)
                    .addField("open_bid_price", openBidPrice)
                    .addField("open_bid_volume", openBidVolume)
                    .addField("open_bid_count", openBidCount)
                    .addField("closed_ask_price", closedAskPrice)
                    .addField("closed_ask_volume", closedAskVolume)
                    .addField("closed_ask_count", closedAskCount)
                    .addField("closed_bid_price", closedBidPrice)
                    .addField("closed_bid_volume", closedBidVolume)
                    .addField("closed_bid_count", closedBidCount)
                    .build());
        }
    }

    public void addTradeMetric(ExchangeType exchangeType, String symbol, BigDecimal askPrice, BigDecimal askVolume, Integer askCount,
                               BigDecimal bidPrice, BigDecimal bidVolume, Integer bidCount) {
        if (ping.get()) {
            influxDB.write(DB_NAME, RETENTION_POLICY, Point.measurement("trade")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("exchangeType", exchangeType.name())
                    .tag("symbol", symbol)
                    .addField("ask_price", askPrice)
                    .addField("ask_volume", askVolume)
                    .addField("ask_count", askCount)
                    .addField("bid_price", bidPrice)
                    .addField("bid_volume", bidVolume)
                    .addField("bid_count", bidCount)
                    .build());
        }
    }


}
