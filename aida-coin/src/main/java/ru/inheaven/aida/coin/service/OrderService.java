package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.dto.trade.LimitOrder;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author inheaven on 12.02.2015 21:06.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OrderService {
    private Map<Long, Map<String, LimitOrder>> orderMap = new ConcurrentHashMap<>();

    public Map<String, LimitOrder> getOrders(Long traderId){
        return orderMap.get(traderId);
    }

    public LimitOrder getOrder(Long traderId, String orderId){
        return null;
    }
}
