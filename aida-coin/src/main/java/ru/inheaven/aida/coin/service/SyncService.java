package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.OrderHistory;
import ru.inheaven.aida.coin.entity.OrderStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 21:58
 */
@Stateless
@Path("/")
public class SyncService {
    @EJB
    private TraderBean traderBean;

    @GET
    @Path("getOrders")
    @Produces("application/json")
    private List<OrderHistory> getOrderHistory(){
        return traderBean.getOrderHistories(ExchangeType.BITTREX, OrderStatus.OPENED);
    }



}
