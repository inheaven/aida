package ru.inheaven.aida.okex.service;

import io.reactivex.schedulers.Schedulers;
import ru.inheaven.aida.okex.fix.OkexExchange;
import ru.inheaven.aida.okex.mapper.InfoMapper;
import ru.inheaven.aida.okex.mapper.OrderMapper;
import ru.inheaven.aida.okex.mapper.PositionMapper;
import ru.inheaven.aida.okex.mapper.TradeMapper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StorageService {
    @Inject
    private OkexExchange okexExchange;

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
        okexExchange.getTrades()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(tradeMapper::insert);

        okexExchange.getOrders()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(orderMapper::insert);

        okexExchange.getPositions()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(positionMapper::insert);

        okexExchange.getInfos()
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(infoMapper::insert);
    }
}
