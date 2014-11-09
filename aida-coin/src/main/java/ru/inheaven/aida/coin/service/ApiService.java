package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.OrderHistory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 21:58
 */
@Stateless
@Path("/")
public class ApiService {
    @EJB
    private TraderBean traderBean;

    @GET
    @Path("getOrders/{startDate}")
    @Produces("application/json")
    private List<OrderHistory> getOrderHistory(@PathParam("startDate") long startDate){
        return traderBean.getOrderHistories(new Date(startDate));
    }



}
