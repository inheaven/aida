package ru.inheaven.aida.happy.trading.service;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author inheaven on 017 17.06.15 18:36
 */
@Singleton
public class StatusService {
    private OkcoinService okcoinService;

    private long tradeCount = 0;
    private long depthCount = 0;
    private long orderCount = 0;

    public StatusService() {
    }

    @Inject
    public StatusService(OkcoinService okcoinService, BroadcastService broadcastService) {
        this.okcoinService = okcoinService;

        okcoinService.getTradeObservable().subscribe(trade -> {
            tradeCount++;

            broadcastService.broadcast(StatusService.class, "UPDATE_TRADE");
        });

        okcoinService.getDepthObservable().subscribe(depth -> {
            depthCount++;

            broadcastService.broadcast(StatusService.class, "UPDATE_DEPTH");

        });

        okcoinService.getOrderObservable().subscribe(order -> {
            orderCount++;

            broadcastService.broadcast(StatusService.class, "UPDATE_ORDER");
        });
    }

    public boolean isMarketDataEndpointOpen(){
        return okcoinService.getMarketDataEndpoint().getSession().isOpen();
    }

    public boolean isTradingEndpointOpen(){
        return okcoinService.getTradingEndpoint().getSession().isOpen();
    }

    public long getTradeCount() {
        return tradeCount;
    }

    public long getDepthCount() {
        return depthCount;
    }

    public long getOrderCount() {
        return orderCount;
    }
}
