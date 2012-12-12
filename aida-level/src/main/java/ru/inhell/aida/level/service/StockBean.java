package ru.inhell.aida.level.service;

import ru.inhell.aida.level.entity.Level;
import ru.inhell.aida.level.entity.Stock;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.12.12 17:09
 */
@Stateless
public class StockBean {
    @PersistenceContext
    private EntityManager em;

    public List<Stock> getStocks(){
        return em.createQuery("select s from Stock s", Stock.class).getResultList();
    }

    public List<Level> getLevels(Long stockId){
        return em.createQuery("select l from Level l where l.stock.id =:stockId", Level.class)
                .setParameter("stockId", stockId)
                .getResultList();
    }
}
