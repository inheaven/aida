package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.*;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

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

    public List<Trader> getLiquidTraders(){
        List<Trader> list =  em.createQuery("select distinct t from Trader t left join BalanceHistory h " +
                "on (h.exchangeType = t.exchange and h.pair = t.pair) where h.date > :date " +
                "group by h.exchangeType, h.pair order by count(h.id) desc", Trader.class)
                .setParameter("date", new Date(System.currentTimeMillis() - 1000*60*60*24)).getResultList();

        List<Trader> all = getTraders();

        for (Trader t : all){
            if (!list.contains(t)){
                list.add(t);
            }
        }

        return list;
    }

    public List<Trader> getTraders(ExchangeType exchangeType){
        List<Trader>  traders = em.createQuery("select t from Trader t where t.exchange = :exchangeType", Trader.class)
                .setParameter("exchangeType", exchangeType)
                .getResultList();

        traders.forEach(new Consumer<Trader>() {
            @Override
            public void accept(Trader trader) {
                em.detach(trader);
            }
        });

        return traders;
    }

    public List<String> getTraderPairs(ExchangeType exchangeType){
        return em.createQuery("select t.pair from Trader t where t.exchange = :exchangeType", String.class)
                .setParameter("exchangeType", exchangeType)
                .getResultList();
    }

    public Long getTradersCount(){
        return em.createQuery("select count(t) from Trader t", Long.class).getSingleResult();
    }

    public Trader getTrader(Long id){
        return em.createQuery("select t from Trader t where t.id = :id", Trader.class).setParameter("id", id).getSingleResult();
    }

    public void save(AbstractEntity abstractEntity){
        if (abstractEntity.getId() == null) {
            em.persist(abstractEntity);
        }else {
            em.merge(abstractEntity);
            em.flush();
            em.clear();
        }
    }

    public List<BalanceHistory> getBalanceHistories(Date startDate){
        return em.createQuery("select h from BalanceHistory h where h.date >= :startDate order by h.date asc", BalanceHistory.class)
                .setParameter("startDate", startDate)
                .getResultList();
    }

    public BigDecimal getSigma(ExchangePair exchangePair){
        return new BigDecimal((Double) em.createNativeQuery("select std(price) from ticker_history " +
                "where exchangetype = ? and pair = ?")
                .setParameter(1, exchangePair.getExchangeType().name())
                .setParameter(2, exchangePair.getPair())
                .getSingleResult());
    }
}
