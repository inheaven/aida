package ru.inhell.aida.web.order;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.wicketstuff.flot.*;
import org.wicketstuff.flot.Color;
import ru.inhell.aida.entity.AlphaTraderData;
import ru.inhell.aida.common.entity.Order;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.quik.OrderBean;
import ru.inhell.aida.trader.AlphaTraderBean;
import ru.inhell.aida.web.AidaFlotPanel;
import ru.inhell.aida.web.Series;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 16.08.11 0:09
 */
public class OrderPage extends WebPage{
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//    SimpleDateFormat day = new SimpleDateFormat("dd.MM.yyyy");

    private AlphaTraderBean alphaTraderBean = AidaInjector.getInstance(AlphaTraderBean.class);

    class Balance{
        float buy = 0;
        int buy_q = 0;

        float sell = 0;
        int sell_q = 0;

        float commission = 0;

        float getBalance(){
            return sell/sell_q - buy/buy_q;
        }
    }



    public Long getId(Order order){
        AlphaTraderData alphaTraderData = alphaTraderBean.getAlphaTraderData(order.getTransactionId());

        return alphaTraderData != null ? alphaTraderData.getAlphaTraderId() : -1;
    }

    public OrderPage() throws ParseException {
        Map<Long, Balance> balanceMap = new HashMap<Long, Balance>();
        Map<Long, List<DataSet>> dataSetMap = new HashMap<Long, List<DataSet>>();

        OrderBean orderBean = AidaInjector.getInstance(OrderBean.class);
        final List<Order> orders = orderBean.getOrders("EDU1", "15.08.2011");
        orders.addAll(orderBean.getOrders("EDU1", "16.08.2011"));
        orders.addAll(orderBean.getOrders("EDU1", "17.08.2011"));
        orders.addAll(orderBean.getOrders("EDU1", "18.08.2011"));
        orders.addAll(orderBean.getOrders("EDU1", "19.08.2011"));


        //init maps
        for (Order order : orders){
            Long id = getId(order);

            if (!balanceMap.containsKey(id)){
                balanceMap.put(id, new Balance());
            }

            if (!dataSetMap.containsKey(id)){
                dataSetMap.put(id, new ArrayList<DataSet>());
            }
        }

        final IModel<List<Series>> model = new ListModel<Series>(new ArrayList<Series>());


        for (int i = 0, ordersSize = orders.size(); i < ordersSize; i++) {
            Order order = orders.get(i);

            Long id = getId(order);
            Balance balance = balanceMap.get(id);

            if (order.getType().equals("Купля")){
                balance.buy += order.getPrice()*order.getQuantity();
                balance.buy_q += order.getQuantity();
            }else{
                balance.sell += order.getPrice()*order.getQuantity();
                balance.sell_q += order.getQuantity();
            }

            balance.commission += order.getQuantity()*1.5;

            dataSetMap.get(id).add(new DataSet(i, balance.getBalance() * 28653 - balance.commission));
        }

        Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, new Color(127,0,255), new Color(255,127,36),
                new Color(173,255,47), new Color(199,21,133), new Color(125,38,205), new Color(139,137,112)};
        int index = 0;
        for (Long id : dataSetMap.keySet()) {
            model.getObject().add(new Series(dataSetMap.get(id), id + "", colors[++index%colors.length], new LineGraphType(null, false, null)));
        }

        final AidaFlotPanel flotPanel = new AidaFlotPanel("flot", model);
        flotPanel.setClickable(false);

        add(flotPanel);
    }
}
