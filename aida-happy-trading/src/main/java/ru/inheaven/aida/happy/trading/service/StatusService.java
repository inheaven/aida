package ru.inheaven.aida.happy.trading.service;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author inheaven on 017 17.06.15 18:36
 */
@Singleton
public class StatusService {
    @Inject
    private OkcoinService okcoinService;

    private long tradeCount = 0;
    private long depthCount = 0;
    private long orderCount = 0;

    public StatusService() {
        okcoinService.getTradeObservable().subscribe(trade -> tradeCount++);
        okcoinService.getDepthObservable().subscribe(depth -> depthCount++);
        okcoinService.getOrderObservable().subscribe(order -> orderCount++);
    }

    public boolean isMarketDataEndpointOpen(){
        return okcoinService.getMarketDataEndpoint().getSession().isOpen();
    }

    public boolean isTradingEndpointOpen(){
        return okcoinService.getTradingEndpoint().getSession().isOpen();
    }
}
