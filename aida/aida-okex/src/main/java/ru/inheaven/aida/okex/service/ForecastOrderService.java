package ru.inheaven.aida.okex.service;

import com.google.common.collect.Lists;
import ru.inheaven.aida.okex.fix.OkexExchange;
import ru.inheaven.aida.okex.mapper.OrderMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov
 * 30.08.2017 20:20
 */
@Singleton
public class ForecastOrderService {
    private Map<String, VSSAOrderService> vssaServiceMap = new HashMap<>();

    @Inject
    private OrderMapper orderMapper;

    @Inject
    private OkexExchange okexExchange;

    @Inject
    public void init(){
//        vssaServiceMap.put("this_weekbtc_usd", new VSSAOrderService(
//                Lists.reverse(orderMapper.getFilledOrders("this_week", "btc_usd", 35*365)),
//                0.5, 100, 10, 365, 12, 1, 1000, 5));

        vssaServiceMap.put("quarterbtc_usd", new VSSAOrderService(
                Lists.reverse(orderMapper.getFilledOrders("quarter", "btc_usd", 35*365)),
                0.5, 100, 10, 365, 12, 1, 1000, 5));

        okexExchange.getOrders()
                .filter(o -> "filled".equals(o.getStatus()))
                .filter(o -> "market".equals(o.getType()) || "limit".equals(o.getType()))
                .subscribe(t -> {
                    VSSAOrderService vssaService = vssaServiceMap.get(t.getSymbol()+t.getCurrency());

                    if (vssaService != null){
                        vssaService.add(t);
                    }
                });
    }

    public double getForecast(String symbol, String currency){
        VSSAOrderService vssaService = vssaServiceMap.get(symbol + currency);

        return vssaService != null ? vssaService.getForecast()/vssaService.getVssaCount() : 0;
    }
}
