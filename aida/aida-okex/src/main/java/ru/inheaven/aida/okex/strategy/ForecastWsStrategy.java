package ru.inheaven.aida.okex.strategy;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.model.Depth;
import ru.inheaven.aida.okex.model.Position;
import ru.inheaven.aida.okex.service.ForecastService;
import ru.inheaven.aida.okex.service.InfluxService;
import ru.inheaven.aida.okex.util.Buffer;
import ru.inheaven.aida.okex.ws.OkexWsExchange;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 * 22.02.2018 10:45
 */
public class ForecastWsStrategy {
    private Logger log = LoggerFactory.getLogger(ForecastWsStrategy.class);

    private Buffer<Position> longPosition  = Buffer.create();
    private Buffer<Position> shortPosition  = Buffer.create();

    @Inject
    private OkexWsExchange okexWsExchange;

    @Inject
    private ForecastService forecastService;

    @Inject
    private InfluxService influxService;

    private String symbol;
    private String currency;

    private Buffer<Depth> depthBuffer = Buffer.create();

    public ForecastWsStrategy(String symbol, String currency) {
        this.symbol = symbol;
        this.currency = currency;
    }

    @SuppressWarnings({"Duplicates", "ResultOfMethodCallIgnored"})
    @Inject
    private void subscribe(){
        //position
        okexWsExchange.getPositions()
                .filter(p -> symbol.equals(p.getSymbol()) && currency.equals(p.getCurrency()))
                .subscribe(p -> {
                    try {
                        if ("long".equals(p.getType())){
                            longPosition.add(p);
                        }else if ("short".equals(p.getType())){
                            shortPosition.add(p);
                        }
                    } catch (Exception e) {
                        log.error("error position", e);
                    }
                });

        okexWsExchange.getDepths()
                .filter(d -> symbol.equals(d.getSymbol()) && currency.equals(d.getCurrency()))
                .subscribe(d -> {
                    try {
                        depthBuffer.add(d);
                    } catch (Exception e) {
                        log.error("error depth", e);
                    }
                });
    }

    @Inject
    public void shedule(){
        //metric
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                influxService.getInfluxDB().write(Point.measurement("okex_strategy")
                        .tag("currency", currency)
                        .tag("symbol", symbol)
                        .addField("long_qty", longPosition.peekLast().getQty())
                        .addField("short_qty", shortPosition.peekLast().getQty())
                        .addField("forecast", forecastService.getForecast(symbol, currency))
                        .addField("delta", longPosition.peekLast().getQty() - shortPosition.peekLast().getQty())
                        .build());
            } catch (Exception e) {
                log.error("error metric schedule ", e);
            }
        }, 90, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                double forecast = forecastService.getForecast(symbol, currency);

                Position longPos =  longPosition.peekLast();
                Position shortPos = shortPosition.peekLast();

                if (longPos != null && shortPos != null) {
                    int delta = longPos.getQty() - shortPos.getQty();

                    if (forecast > 0 && delta < 0){
                        String price = depthBuffer.peekLast().getAsk().multiply(new BigDecimal("1.01")).toPlainString();

                        if (shortPos.getEup() > 0) {
                            okexWsExchange.futureTrade(currency, symbol, price, shortPos.getEup(),4, 0, 20);
                        }

                        okexWsExchange.futureTrade(currency, symbol, price, 1,1, 0, 20);
                    }else if (forecast < 0 && delta >= 0){
                        String price = depthBuffer.peekLast().getBid().multiply(new BigDecimal("0.99")).toPlainString();

                        if (longPos.getEup() > 0) {
                            okexWsExchange.futureTrade(currency, symbol, price, longPos.getEup(),3, 0, 20);
                        }

                        okexWsExchange.futureTrade(currency, symbol, price, 1,2, 0, 20);
                    }
                }
            } catch (Exception e) {
                log.error("error trade", e);
            }
        }, 90, 10, TimeUnit.SECONDS);
    }
}
