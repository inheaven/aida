package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.*;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collections;
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

    public List<Trader> getTraders(ExchangeType exchangeType){
        List<Trader>  traders = em.createQuery("select t from Trader t where t.exchangeType = :exchangeType", Trader.class)
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
        return em.createQuery("select t.pair from Trader t where t.exchangeType = :exchangeType", String.class)
                .setParameter("exchangeType", exchangeType)
                .getResultList();
    }

    public Long getTradersCount(){
        return em.createQuery("select count(t) from Trader t", Long.class).getSingleResult();
    }

    public Long getBalanceHistoryCount(Date startDate){
        return em.createQuery("select count(h) from BalanceHistory h where h.date >= :startDate", Long.class)
                .setParameter("startDate", startDate)
                .getSingleResult();
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

    public List<BalanceHistory> getBalanceHistories(ExchangePair exchangePair, Date startDate){
        if (exchangePair != null){
            return em.createQuery("select h from BalanceHistory h where h.pair = :pair and h.exchangeType = :exchangeType " +
                    "and h.date >= :startDate order by h.date asc", BalanceHistory.class)
                    .setParameter("pair", exchangePair.getPair())
                    .setParameter("exchangeType", exchangePair.getExchangeType())
                    .setParameter("startDate", startDate)
                    .getResultList();
        }else {
            return em.createQuery("select h from BalanceHistory h where h.date >= :startDate order by h.date asc", BalanceHistory.class)
                    .setParameter("startDate", startDate)
                    .getResultList();
        }
    }

    public BigDecimal getSigma(ExchangePair exchangePair){
        try {
            return new BigDecimal((Double) em.createNativeQuery("select std(price) from ticker_history " +
                    "where exchange_type = ? and pair = ? and `date` >  DATE_SUB(NOW(), INTERVAL 12 HOUR)")
                    .setParameter(1, exchangePair.getExchangeType().name())
                    .setParameter(2, exchangePair.getPair())
                    .getSingleResult());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getAverage(ExchangePair exchangePair){
        try {
            return (BigDecimal) em.createNativeQuery("select avg(price) from ticker_history " +
                    "where exchange_type = ? and pair = ? and `date` >  DATE_SUB(NOW(), INTERVAL 72 HOUR)")
                    .setParameter(1, exchangePair.getExchangeType().name())
                    .setParameter(2, exchangePair.getPair())
                    .getSingleResult();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
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

    public List<Order> getOrderHistories(ExchangePair exchangePair, OrderStatus status, Date startDate){
        return em.createQuery("select h from Order h where h.exchangeType = :exchangeType and " +
                "h.pair = :pair and  h.status = :status and " +
                "(h.opened > :startDate or (h.closed is not null and h.closed > :startDate))", Order.class)
                .setParameter("exchangeType", exchangePair.getExchangeType())
                .setParameter("pair", exchangePair.getPair())
                .setParameter("status", status)
                .setParameter("startDate", startDate)
                .getResultList();
    }

    public List<Order> getOrderHistories(ExchangeType exchangeType, OrderStatus status){
        return em.createQuery("select h from Order h where h.exchangeType = :exchangeType and " +
                "h.status = :status", Order.class)
                .setParameter("exchangeType", exchangeType)
                .setParameter("status", status)
                .getResultList();
    }

    public Long getOrderHistoryCount(Date startDate, OrderStatus status){
        return em.createQuery("select count(h) from Order h where (h.opened > :startDate or (h.closed is not null and " +
                "h.closed > :startDate)) and h.status = :status", Long.class)
                .setParameter("startDate", startDate)
                .setParameter("status", status)
                .getSingleResult();
    }

    public List<Order> getOrderHistories(Date startDate){
        return em.createQuery("select h from Order h where h.opened > :startDate or (h.closed is not null and " +
                "h.closed > :startDate)", Order.class)
                .setParameter("startDate", startDate)
                .getResultList();
    }

    public List<Order> getOrderHistories(OrderStatus status, Date startDate){
        return em.createQuery("select h from Order h where (h.opened > :startDate or (h.closed is not null and " +
                "h.closed > :startDate)) and h.status = :status", Order.class)
                .setParameter("startDate", startDate)
                .setParameter("status", status)
                .getResultList();
    }

    public Order getOrderHistory(String orderId){
        try {
            return em.createQuery("select h from Order h where h.orderId = :orderId", Order.class)
                    .setParameter("orderId", orderId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<OrderStat> getOrderStats(Date startDate) {
        return em.createNativeQuery("SELECT id, sum(price * filled_amount)/sum(filled_amount) AS avg_price, " +
                "sum(filled_amount) AS sum_amount, type, exchange_type, pair FROM `order` " +
                "WHERE status='CLOSED' and CLOSED > ? GROUP BY pair, exchange_type, type", OrderStat.class)
                .setParameter(1, startDate)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<OrderStat> getOrderStats(ExchangePair exchangePair, Date startDate) {
        return em.createNativeQuery("SELECT id, sum(price * filled_amount)/sum(filled_amount) AS avg_price, " +
                "sum(filled_amount) AS sum_amount, type, exchange_type, pair FROM `order` " +
                "WHERE exchange_type = ? and pair = ? and status='CLOSED' and CLOSED > ? GROUP BY type", OrderStat.class)
                .setParameter(1, exchangePair.getExchangeType().name())
                .setParameter(2, exchangePair.getPair())
                .setParameter(3, startDate)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<OrderStat> getOrderStatVolume(ExchangePair exchangePair, Date startDate){
        return em.createNativeQuery("select id, round(price,2) as avg_price, " +
                "sum(filled_amount) as sum_amount, type, exchange_type, pair FROM `order` " +
                "WHERE exchange_type = ? and pair = ? and status='CLOSED' and CLOSED > ? GROUP BY round(price, 2)", OrderStat.class)
                .setParameter(1, exchangePair.getExchangeType().name())
                .setParameter(2, exchangePair.getPair())
                .setParameter(3, startDate)
                .getResultList();
    }

    public List<Equity> getEquities(Date startDate){
        return em.createQuery("select e from Equity e where e.date > :startDate and e.exchangeType is null", Equity.class)
                .setParameter("startDate", startDate)
                .getResultList();
    }

    public List<Equity> getEquities(ExchangeType exchangeType, Date startDate){
        return em.createQuery("select e from Equity e where e.date > :startDate and e.exchangeType = :exchangeType", Equity.class)
                .setParameter("exchangeType", exchangeType)
                .setParameter("startDate", startDate)
                .getResultList();
    }
}
