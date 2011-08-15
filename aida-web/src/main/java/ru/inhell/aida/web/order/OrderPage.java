package ru.inhell.aida.web.order;

import com.googlecode.charts4j.*;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.wicketstuff.flot.*;
import ru.inhell.aida.entity.Order;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.quik.OrderBean;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.web.AidaFlotPanel;
import ru.inhell.aida.web.Series;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 16.08.11 0:09
 */
public class OrderPage extends WebPage{
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//    SimpleDateFormat day = new SimpleDateFormat("dd.MM.yyyy");

    public OrderPage() throws ParseException {

        OrderBean orderBean = AidaInjector.getInstance(OrderBean.class);
        final List<Order> orders = orderBean.getOrders("EDU1", "15.08.2011");

        float buy = 0;
        int buy_q = 0;

        float sell = 0;
        int sell_q = 0;

        final IModel<List<Series>> model = new ListModel<Series>(new ArrayList<Series>());

        List<DataSet> dataSets = new ArrayList<DataSet>();

        for (int i = 0, ordersSize = orders.size(); i < ordersSize; i++) {
            Order order = orders.get(i);

            if (order.getType().equals("Купля")){
                buy += order.getPrice()*order.getQuantity();
                buy_q += order.getQuantity();
            }else{
                sell += order.getPrice()*order.getQuantity();
                sell_q += order.getQuantity();
            }

            float balance = sell/sell_q - buy/buy_q;

//            long time = (sdf.parse(order.getTime()).getTime())/1000/60;

//            dataSets.add(new DataSet(time, balance));
            dataSets.add(new DataSet(i, balance));

        }

        model.getObject().add(new Series(dataSets, "balance", org.wicketstuff.flot.Color.BLACK, new LineGraphType(null, false, null)));

        final AidaFlotPanel flotPanel = new AidaFlotPanel("flot", model);
        flotPanel.setClickable(false);

        add(flotPanel);
    }
}
