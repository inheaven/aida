package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.web.HighstockPage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

/**
 * @author inheaven on 26.10.2015 21:46.
 */
public class TickPage extends HighstockPage {
    public TickPage() {
        add(new BroadcastBehavior<Trade>(TradeService.class){
            private Long time = System.currentTimeMillis();

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Trade t) {
                if (t.getSymbol().equals("BTC/CNY")){
                    handler.appendJavaScript("tick_chart.series[" + ( t.getOrderType().equals(OrderType.BID) ? 0 : 1 ) + "]" +
                            ".addPoint([" + t.getCreated().getTime() + "," +  t.getPrice() + "], false);");

                    handler.appendJavaScript("tick_chart.series[4].addPoint([" + t.getCreated().getTime() + "," +  t.getAmount() + "], false);");

                    if (System.currentTimeMillis() - time > 500){
                        handler.appendJavaScript("tick_chart.redraw();");

                        time = System.currentTimeMillis();
                    }
                }
            }
        });

        add(new BroadcastBehavior<Order>(OrderService.class){
            private Long time = System.currentTimeMillis();

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Order o) {
                if (key.equals("close") && o.getSymbol().equals("BTC/CNY")){
                    handler.appendJavaScript("tick_chart.series[" + ( o.getType().equals(OrderType.BID) ? 2 : 3 ) + "]" +
                            ".addPoint([" + o.getCreated().getTime() + "," +  o.getAvgPrice() + "], false);");

                    handler.appendJavaScript("tick_chart.series[4].addPoint([" + o.getCreated().getTime() + "," +  o.getAmount() + "], false);");

                    if (System.currentTimeMillis() - time > 500){
                        handler.appendJavaScript("tick_chart.redraw();");

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
