package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.web.HighstockPage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
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
            double amount = Module.getInjector().getInstance(StrategyMapper.class).getStrategies().stream()
                    .filter(s -> s.getId().equals(45L))
                    .map(Strategy::getLevelLot)
                    .findAny()
                    .orElse(BigDecimal.ZERO).doubleValue();
            long last = System.currentTimeMillis();

            private Map<Long, BigDecimal> profitMap = new HashMap<>();

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Order o) {
                if (key.equals("close") && o.getSymbol().equals("BTC/CNY") && o.getStatus().equals(OrderStatus.CLOSED)){
                    profitMap.put(o.getStrategyId(), o.getSellPrice().multiply(o.getSellVolume())
                            .subtract(o.getBuyPrice().multiply(o.getBuyVolume()))
                            .divide(o.getBuyVolume().add(o.getSellVolume()), RoundingMode.HALF_UP));

                    count++;

                    double c = o.getAmount().doubleValue();

                    int d = 255;
                    int l = c < amount ? 10 : 0;

                    String color = o.getType().equals(OrderType.BID)
                            ? "rgb(" + l + " , " + d + ", " + l + ")"
                            : "rgb(" + d + " , " + l + ", " + l + ")";

                    queue.add("tick_chart.series[0]" +
                            ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  o.getAvgPrice() + ", marker: {fillColor: '"+ color +"'}}, " +
                            "false, " + (count >1500 ? "true" : "false") + ");");

                    if (System.currentTimeMillis() - last > 1000){
                        last = System.currentTimeMillis();

                        BigDecimal profit = profitMap.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

                        queue.add("tick_chart.series[1]" +
                                ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  profit + ", marker: {fillColor: '#ffff00'}}, " +
                                "false, " + (count >1500 ? "true" : "false") + ");");

//                        queue.add("tick_chart.series[0]" +
//                                ".addPoint({x:" + o.getClosed().getTime() + ", y:" +  o.getSellPrice() + ", marker: {fillColor: '#ff8000'}}, " +
//                                "false, " + (count >1500 ? "true" : "false") + ");");
                    }

                    if (System.currentTimeMillis() - time > 40){
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
