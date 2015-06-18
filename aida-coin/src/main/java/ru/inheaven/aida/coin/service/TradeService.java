package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.Trade;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * @author inheaven on 01.05.2015 9:11.
 */
@Singleton
public class TradeService {
    @EJB
    private EntityBean entityBean;

    private PublishSubject<Trade> tradeSubject = PublishSubject.create();

    @PostConstruct
    public void start(){
        tradeSubject.subscribe(entityBean::save);
    }

    public Subject<Trade, Trade> getTradeSubject(){
        return tradeSubject;
    }
}
