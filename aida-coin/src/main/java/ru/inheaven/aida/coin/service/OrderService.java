package ru.inheaven.aida.coin.service;

import com.google.common.base.Throwables;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.Order;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.inheaven.aida.coin.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.coin.entity.OrderStatus.OPENED;

/**
 * @author inheaven on 12.02.2015 21:06.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OrderService extends AbstractService{
    private Map<Long, Map<String, Order>> orderMap = new ConcurrentHashMap<>();

    public Map<String, Order> getOrders(Long traderId){
        return orderMap.get(traderId);
    }

    public OpenOrders getOpenOrders(ExchangeType exchangeType){
        return null; //todo implement open orders by exchange type
    }

    private void updateOpenOrders(ExchangeType exchangeType) throws IOException {
//        OpenOrders openOrders = getExchange(exchangeType).getPollingTradeService().getOpenOrders();
//        openOrdersMap.put(exchangeType, openOrders);
//
//        broadcast(exchangeType, openOrders);
    }

    public void updateClosedOrders(ExchangeType exchangeType){
        OpenOrders openOrders = orderService.getOpenOrders(exchangeType);

        for (Order h : traderBean.getOrderHistories(exchangeType, OPENED)) {
            if (openOrders == null || System.currentTimeMillis() - h.getOpened().getTime() < 60000){
                continue;
            }

            try {
                boolean found = false;

                for (LimitOrder o : openOrders.getOpenOrders()){
                    if (o.getId().split("&")[0].equals(h.getOrderId())){
                        found = true;
                        break;
                    }
                }

                if (!found){
                    h.setStatus(CLOSED);
                    h.setFilledAmount(h.getTradableAmount());
                    h.setClosed(new Date());

                    entityBean.save(h);
                    broadcast(exchangeType, h);
                }
            } catch (Exception e) {
                log.error("updateClosedOrders error", e);

                //noinspection ThrowableResultOfMethodCallIgnored
                broadcast(exchangeType, exchangeType.name() + ": " + Throwables.getRootCause(e).getMessage());
            }
        }
    }
}
