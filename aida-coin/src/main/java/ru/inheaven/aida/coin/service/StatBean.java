package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.TickerHistory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author inheaven on 16.02.2015 23:40.
 */
@Stateless
public class StatBean {
    @PersistenceContext
    private EntityManager em;

    public BigDecimal getVolatilitySigma(ExchangePair exchangePair){
        return new BigDecimal((Double) em.createNativeQuery("select std(price) from ticker_history " +
                "where exchange_type = ? and pair = ? and `date` >  DATE_SUB(NOW(), INTERVAL 12 HOUR)")
                .setParameter(1, exchangePair.getExchangeType().name())
                .setParameter(2, exchangePair.getPair())
                .getSingleResult());
    }

    public BigDecimal getAverage(ExchangePair exchangePair){
        return (BigDecimal) em.createNativeQuery("select avg(price) from ticker_history " +
                "where exchange_type = ? and pair = ? and `date` >  DATE_SUB(NOW(), INTERVAL 72 HOUR)")
                .setParameter(1, exchangePair.getExchangeType().name())
                .setParameter(2, exchangePair.getPair())
                .getSingleResult();
    }

    public List<TickerHistory> getTickerHistories(ExchangePair exchangePair, int count){
        List<TickerHistory> list =  em.createQuery("select th from TickerHistory th where th.pair = :pair and th.exchangeType = :exchangeType " +
                "order by th.date desc", TickerHistory.class)
                .setParameter("pair", exchangePair.getPair())
                .setParameter("exchangeType", exchangePair.getExchangeType())
                .setMaxResults(count)
                .getResultList();

        Collections.reverse(list);

        return list;
    }

    public List<TickerHistory> getTickerHistories(ExchangePair exchangePair, Date startDate){
        return em.createQuery("select th from TickerHistory th where th.pair = :pair " +
                "and th.exchangeType = :exchangeType and th.date > :startDate", TickerHistory.class)
                .setParameter("pair", exchangePair.getPair())
                .setParameter("exchangeType", exchangePair.getExchangeType())
                .setParameter("startDate", startDate)
                .getResultList();
    }
}
