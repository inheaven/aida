package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.Order;
import ru.inheaven.aida.coin.entity.OrderStatus;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import java.util.List;

/**
 * @author inheaven on 03.05.2015 2:09.
 */
@Stateless
public class OrderBean extends EntityBean{
    @PersistenceUnit
    private EntityManager entityManager;

    public List<Order> getOrders(OrderStatus status){
        return entityManager.createQuery("select o from Order o where o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList();
    }

    public Order getOrder(String orderId){
        return entityManager.createQuery("select o from Order o where o.orderId = :orderId", Order.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }
}
