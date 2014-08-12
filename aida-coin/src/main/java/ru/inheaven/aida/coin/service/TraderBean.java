package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.currency.CurrencyPair;
import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.Trader;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         08.01.14 15:22
 */
@Stateless
public class TraderBean {
    @PersistenceContext
    private EntityManager em;

    public List<Trader> getTraders(){
        return em.createQuery("select t from Trader t", Trader.class).getResultList();
    }

    public List<Trader> getTraders(ExchangeType exchangeType){
        return em.createQuery("select t from Trader t where t.exchange = :exchangeType", Trader.class)
                .setParameter("exchangeType", exchangeType)
                .getResultList();
    }

    public List<String> getTraderPairs(ExchangeType exchangeType){
        return em.createQuery("select t.pair from Trader t where t.exchange = :exchangeType", String.class)
                .setParameter("exchangeType", exchangeType)
                .getResultList();
    }

    public Trader getTrader(Long id){
        return em.createQuery("select t from Trader t where t.id = :id", Trader.class).setParameter("id", id).getSingleResult();
    }

    public void save(Trader trader){
        if (trader.getId() == null) {
            em.persist(trader);
        }else {
            em.merge(trader);
            em.flush();
        }
    }
}
