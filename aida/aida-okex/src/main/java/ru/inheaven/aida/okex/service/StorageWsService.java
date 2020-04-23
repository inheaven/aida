package ru.inheaven.aida.okex.service;

import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.mapper.InfoMapper;
import ru.inheaven.aida.okex.mapper.OrderMapper;
import ru.inheaven.aida.okex.mapper.PositionMapper;
import ru.inheaven.aida.okex.mapper.TradeMapper;
import ru.inheaven.aida.okex.ws.OkexWsExchange;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StorageWsService {
    private Logger log = LoggerFactory.getLogger(StorageWsService.class);

    @Inject
    private OkexWsExchange okexWsExchange;

    @Inject
    private TradeMapper tradeMapper;

    @Inject
    private OrderMapper orderMapper;

    @Inject
    private InfoMapper infoMapper;

    @Inject
    private PositionMapper positionMapper;

    @Inject
    public void init(){
        okexWsExchange.getTrades()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(tradeMapper::insert, t -> log.error("error", t));

        okexWsExchange.getOrders()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(orderMapper::insert, t -> log.error("error", t));

        okexWsExchange.getPositions()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(positionMapper::insert, t -> log.error("error", t));

        okexWsExchange.getInfos()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(infoMapper::insert, t -> log.error("error", t));
    }
}
