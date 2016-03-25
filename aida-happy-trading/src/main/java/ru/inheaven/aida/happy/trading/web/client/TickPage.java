package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.service.UserInfoService;
import ru.inheaven.aida.happy.trading.web.HighstockPage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author inheaven on 26.10.2015 21:46.
 */
public class TickPage extends HighstockPage {
    private volatile Long time = System.currentTimeMillis();
    private volatile Integer count = 0;

    Queue<String> queue = new ConcurrentLinkedQueue<>();

    public TickPage() {
        add(new BroadcastBehavior<Trade>(TradeService.class){
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Trade t) {

            }
        });

        add(new BroadcastBehavior<Order>(OrderService.class){
            long last = System.currentTimeMillis();

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Order o) {
                if (key.equals("close") && o.getSymbol().equals("BTC/CNY") && o.getStatus().equals(OrderStatus.CLOSED)){
                    count++;

                    String color = o.getType().equals(OrderType.BID) ? "#90ee7e" : "#f45b5b";

                    queue.add("tick_chart.series[0]" +
                            ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  o.getAvgPrice() + ", marker: {fillColor: '"+ color +"'}}, " +
                            "false, " + (count >1500 ? "true" : "false") + ");");

                    last = System.currentTimeMillis();

                    queue.add("tick_chart.series[1]" +
                            ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  o.getProfit() + "}, " +
                            "false, " + (count >1500 ? "true" : "false") + ");");
                    queue.add("tick_chart.series[2]" +
                            ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  o.getSpotBalance() + "}, " +
                            "false, " + (count >1500 ? "true" : "false") + ");");

                    UserInfoService userInfoService = Module.getInjector().getInstance(UserInfoService.class);

                    queue.add("tick_chart.series[3]" +
                            ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  userInfoService.getVolume("net", o.getAccountId(), null) + "}, " +
                            "false, " + (count >1500 ? "true" : "false") + ");");

                    if (System.currentTimeMillis() - time > 500){
                        String s;

                        while ((s = queue.poll()) != null){
                            handler.appendJavaScript(s);
                        }

                        handler.appendJavaScript("tick_chart.redraw(false);");

                        time = System.currentTimeMillis();
                    }
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forUrl("./js/tick.js"));
    }
}
