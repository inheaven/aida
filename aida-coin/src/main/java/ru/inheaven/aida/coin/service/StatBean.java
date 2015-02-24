package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.ExchangePair;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

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
}
