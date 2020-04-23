package ru.inheaven.aida.okex.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.mapper.TradeMapper;
import ru.inheaven.aida.okex.ws.OkexWsExchange;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov
 * 30.08.2017 20:20
 */
@Singleton
public class ForecastService {
    private Logger log = LoggerFactory.getLogger(ForecastService.class);

    private Map<String, VSSAService> vssaServiceMap = new HashMap<>();

    @Inject
    private TradeMapper tradeMapper;

    @Inject
    private OkexWsExchange okexWsExchange;

    @Inject
    public void init(){
//        Executors.newWorkStealingPool().execute(() ->{
//            try {
//                vssaServiceMap.put("this_weekbtc_usd", new VSSAService(
//                        Lists.reverse(tradeMapper.getLastTrades("this_week", "btc_usd", 7*300*1000)),
//                        0.5, 100, 10, 300, 3, 1000, 10000, 5));
//
//                okexWsExchange.getTrades().subscribe(t -> {
//                    try {
//                        VSSAService vssaService = vssaServiceMap.get(t.getSymbol()+t.getCurrency());
//
//                        if (vssaService != null){
//                            vssaService.add(t);
//                        }
//                    } catch (Exception e) {
//                        log.error("error vssa subscribe", e);
//                    }
//                });
//            } catch (Exception e) {
//                log.error("error vssa schedule", e);
//            }
//        });

    }

    public double getForecast(String symbol, String currency){
        VSSAService vssaService = vssaServiceMap.get(symbol + currency);

        return vssaService != null ? (vssaService.getForecast()/vssaService.getVssaCount() > 0 ? 1 : -1) : 0;
    }
}
