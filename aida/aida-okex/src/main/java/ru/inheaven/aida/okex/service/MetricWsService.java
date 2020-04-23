package ru.inheaven.aida.okex.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.model.Info;
import ru.inheaven.aida.okex.model.Order;
import ru.inheaven.aida.okex.model.Position;
import ru.inheaven.aida.okex.model.Trade;
import ru.inheaven.aida.okex.ws.OkexWsExchange;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@SuppressWarnings("Duplicates")
@Singleton
public class MetricWsService {
    private Logger log = LoggerFactory.getLogger(MetricWsService.class);

    @Inject
    private OkexWsExchange okexWsExchange;

    @Inject
    private InfluxService influxService;

    private Map<String, Trade> tradeMap = new ConcurrentHashMap<>();
    private Map<String, Position> positionMap = new ConcurrentHashMap<>();
    private Map<String, Info> infoMap = new ConcurrentHashMap<>();


    @Inject
    private void subscribe(){
        okexWsExchange.getTrades()
                .subscribe(t -> {
                    try {
                        tradeMap.put(t.getSymbol() + t.getCurrency(), t);

                        influxService.getInfluxDB().write(Point.measurement("okex_trade")
                                .tag("symbol", t.getSymbol())
                                .tag("currency", t.getCurrency())
                                .addField("price", t.getPrice())
                                .addField("side", t.getSide())
                                .addField("qty", t.getQty())
                                .build());

                        writeBtcEquity();
                        writeLtcEquity();
                    } catch (Exception e) {
                        log.error("error write trade metric ", e);
                    }
                });

        okexWsExchange.getInfos().subscribe( i -> infoMap.put(i.getCurrency(), i));

        okexWsExchange.getPositions()
                .subscribe(p -> {
                    try {
                        //position
                        if (tradeMap.get(p.getSymbol() + p.getCurrency()) != null) {
                            Trade t = tradeMap.get(p.getSymbol() + p.getCurrency());

                            int margin = p.getCurrency().contains("btc") ? 100 : 10;

                            double equity = "long".equals(p.getType())
                                    ? (margin / p.getAvgPrice().doubleValue() - margin / t.getPrice().doubleValue())*p.getQty() + p.getProfit().doubleValue()
                                    : (margin / t.getPrice().doubleValue() - margin / p.getAvgPrice().doubleValue())*p.getQty() + p.getProfit().doubleValue();

                            influxService.getInfluxDB().write(Point.measurement("okex_position")
                                    .tag("symbol", p.getSymbol())
                                    .tag("currency", p.getCurrency())
                                    .tag("type", p.getType())
                                    .addField("avg_price", p.getAvgPrice())
                                    .addField("profit", p.getProfit())
                                    .addField("frozen", p.getFrozen())
                                    .addField("qty", p.getQty())
                                    .addField("evening_up", p.getEveningUp())
                                    .addField("trade_price", tradeMap.get(p.getSymbol() + p.getCurrency()).getPrice())
                                    .addField("equity", equity)
                                    .build());

                            positionMap.put(p.getSymbol() + p.getCurrency() + p.getType(), p);
                        }
                    } catch (Exception e) {
                        log.error("error write position metric ", e);
                    }
                });

        Cache<String, Order> orderCache = CacheBuilder.newBuilder().expireAfterWrite(1, MINUTES).build();

        okexWsExchange.getOrders()
                .filter(o -> {
                    Order order = orderCache.getIfPresent(o.getOrderId());

                    return order == null || !o.getStatus().equals(order.getStatus());
                })
                .subscribe(o -> {
                    try {
                        BigDecimal price = o.getPrice();

                        if (price == null){
                            try {
                                price = new BigDecimal(o.getClOrderId().split(":")[1]);
                            } catch (Exception e) {
                                //
                            }
                        }

                        influxService.getInfluxDB().write(Point.measurement("okex_order")
                                .tag("symbol", o.getSymbol())
                                .tag("currency", o.getCurrency())
                                .tag("type", o.getType() != null ? o.getType() : "")
//                                .tag("side", o.getSide())
                                .tag("status", o.getStatus())
                                .addField("price", price)
                                .addField("avg_price", o.getAvgPrice())
                                .addField("qty", o.getQty())
                                .addField("total_qty", o.getTotalQty())
                                .addField("commission", o.getCommission())
                                .build());

                        orderCache.put(o.getOrderId(), o);
                    } catch (Exception e) {
                        log.error("error order metric ", e);
                    }
                });
    }

    private void writeBtcEquity(){
        List<String> btcKeys = Arrays.asList(
                "this_weekbtc_usdlong",
                "this_weekbtc_usdshort",
                "next_weekbtc_usdlong",
                "next_weekbtc_usdshort",
                "quarterbtc_usdlong",
                "quarterbtc_usdshort"
 );

        double equity = 0;
        boolean calculated = true;

        for (String key : btcKeys){
            Position p = positionMap.get(key);

            if (p == null){
                calculated = false;
                break;
            }

            Trade t = tradeMap.get(p.getSymbol() + p.getCurrency());

            equity += "long".equals(p.getType())
                    ? (100 / p.getAvgPrice().doubleValue() - 100 / t.getPrice().doubleValue())*p.getQty()
                    : (100 / t.getPrice().doubleValue() - 100 / p.getAvgPrice().doubleValue())*p.getQty();
        }

        if (calculated){
            Info info = infoMap.get("btc_usd");

            if (info != null) {
                equity += info.getBalance().doubleValue() + info.getProfit().doubleValue();

                influxService.getInfluxDB().write(Point.measurement("okex_equity")
                        .time(System.currentTimeMillis(), MILLISECONDS)
                        .tag("currency", "btc_usd")
                        .addField("equity", equity)
                        .addField("price_quarter", tradeMap.get("quarterbtc_usd").getPrice())
                        .addField("price_this_week", tradeMap.get("this_weekbtc_usd").getPrice())
                        .build());
            }
        }
    }

    private void writeLtcEquity(){
        List<String> ltcKeys = Arrays.asList(
//                "this_weekltc_usdlong", "this_weekltc_usdshort",
//                "next_weekltc_usdlong", "next_weekltc_usdshort",
                "quarterltc_usdlong", "quarterltc_usdshort");

        double equity = 0;
        boolean calculated = true;

        for (String k : ltcKeys){
            Position p = positionMap.get(k);

            if (p == null){
                calculated = false;
                break;
            }

            Trade t = tradeMap.get(p.getSymbol() + p.getCurrency());

            equity += "long".equals(p.getType())
                    ? (10 / p.getAvgPrice().doubleValue() - 10 / t.getPrice().doubleValue())*p.getQty()
                    : (10 / t.getPrice().doubleValue() - 10 / p.getAvgPrice().doubleValue())*p.getQty();
        }

        if (calculated){
            Info info = infoMap.get("ltc_usd");

            if (info != null) {
                equity += info.getBalance().doubleValue() + info.getProfit().doubleValue();

                influxService.getInfluxDB().write(Point.measurement("okex_equity")
                        .time(System.currentTimeMillis(), MILLISECONDS)
                        .tag("currency", "ltc_usd")
                        .addField("equity", equity)
                        .addField("price_quarter", tradeMap.get("quarterltc_usd").getPrice())
                        .build());
            }
        }
    }
}
