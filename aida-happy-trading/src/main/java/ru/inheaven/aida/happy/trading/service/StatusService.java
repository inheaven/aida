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

        okcoinService.getMarketDataHeartbeatObservable().subscribe(l -> {
            broadcastService.broadcast(StatusService.class, "update_market", l);
        });

        okcoinService.getTradingHeartbeatObservable().subscribe(l -> {
            broadcastService.broadcast(StatusService.class, "update_trading", l);
        });

        okcoinService.getTradeObservable().subscribe(trade -> {
            tradeCount++;

            broadcastService.broadcast(StatusService.class, "update_trade", tradeCount);
        });

        okcoinService.getDepthObservable().subscribe(depth -> {
            depthCount++;

            broadcastService.broadcast(StatusService.class, "update_depth", depthCount);

        });

        okcoinService.getOrderObservable().subscribe(order -> {
            orderCount++;

            broadcastService.broadcast(StatusService.class, "update_order", orderCount);
        });
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
