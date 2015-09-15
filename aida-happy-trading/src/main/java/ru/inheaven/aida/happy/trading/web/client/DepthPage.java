package ru.inheaven.aida.happy.trading.web.client;


import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.SymbolType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.StrategyService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.json.Json;
import javax.json.JsonArray;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 12.08.2015 21:12.
 */
public class DepthPage extends BasePage{
    private static Map<String, Cell> cacheMap = new ConcurrentHashMap<>();
    private Map<String, BigDecimal> tradeMap = new ConcurrentHashMap<>();

    public DepthPage() {
        setVersioned(false);

        add(getListView("ltc_depth"));
        add(getListView("ltc_this_depth"));
        add(getListView("ltc_cn_depth"));
        add(getListView("btc_depth"));
        add(getListView("btc_this_depth"));
        add(getListView("btc_cn_depth"));

        add(new BroadcastBehavior<Depth>(DepthService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Depth depth) {
                String id = getDepthId(depth.getSymbol(), depth.getSymbolType());

                if (id != null){
                    update(id, handler, depth, Module.getInjector().getInstance(StrategyService.class)
                            .getStrategies(depth.getExchangeType(), depth.getSymbol(), depth.getSymbolType()));
                }
            }
        });

        add(new BroadcastBehavior<Trade>(TradeService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Trade trade) {
                String id = getDepthId(trade.getSymbol(), trade.getSymbolType());
                String k = id+trade.getPrice().setScale(3, HALF_UP);
                BigDecimal amount = tradeMap.get(k);

                //noinspection RedundantStringConstructorCall
                tradeMap.put(new String(k), trade.getAmount().add(amount != null ? amount : BigDecimal.ZERO).setScale(3, HALF_UP));
            }
        });
    }

    @SuppressWarnings("Duplicates")
    private String getDepthId(String symbol, SymbolType symbolType) {
        if (symbolType == null){
            if (symbol.equals("LTC/CNY")){
                return "ltc_cn_depth";
            }else if (symbol.equals("BTC/CNY")){
                return "btc_cn_depth";
            }
        }

        if (symbol.equals("LTC/USD")) {
            if (symbolType== null) {
                return "ltc_depth";
            }else if (symbolType.equals(SymbolType.THIS_WEEK)){
                return "ltc_this_depth";
            }else if (symbolType.equals(SymbolType.NEXT_WEEK)){
                return "ltc_next_depth";
            }
        }else if (symbol.equals("BTC/USD")){
            if (symbolType == null) {
                return "btc_depth";
            }else if (symbolType.equals(SymbolType.THIS_WEEK)){
                return "btc_this_depth";
            }else if (symbolType.equals(SymbolType.NEXT_WEEK)){
                return "btc_next_depth";
            }
        }

        return null;
    }

    private class Cell implements Serializable{
        private BigDecimal price;
        private BigDecimal volume;
        private BigDecimal open;

        public Cell(BigDecimal price, BigDecimal volume, BigDecimal open) {
            this.price = price;
            this.volume = volume;
            this.open = open;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (price != null ? !price.equals(cell.price) : cell.price != null) return false;
            if (volume != null ? !volume.equals(cell.volume) : cell.volume != null) return false;
            return !(open != null ? !open.equals(cell.open) : cell.open != null);

        }
    }

    private void update(String id, WebSocketRequestHandler handler, Depth depth, List<BaseStrategy> ltcSpot) {
        Map<BigDecimal, BigDecimal> map = new HashMap<>();

        Json.createReader(new StringReader(depth.getAskJson())).readArray()
                .forEach(j -> {
                    JsonArray a = (JsonArray) j;
                    map.put(a.getJsonNumber(0).bigDecimalValue(), a.getJsonNumber(1).bigDecimalValue());
                });

        Json.createReader(new StringReader(depth.getBidJson())).readArray()
                .forEach(j -> {
                    JsonArray a = (JsonArray) j;
                    map.put(a.getJsonNumber(0).bigDecimalValue(), a.getJsonNumber(1).bigDecimalValue());
                });

        List<BigDecimal> prices = map.keySet().stream().sorted((p1, p2) -> p2.compareTo(p1)).collect(Collectors.toList());

        int index = 0;
        for (BigDecimal price : prices){
            double open = ltcSpot.parallelStream()
                    .map(BaseStrategy::getOrderMap)
                    .flatMap(m -> m.values().stream())
                    .filter(o -> o.getPrice().setScale(3, HALF_UP).equals(price.setScale(3, HALF_UP)) &&
                            o.getStatus().equals(OrderStatus.OPEN))
                    .collect(Collectors.summingDouble(o -> o.getAmount().doubleValue()));

            int volumeScale = depth.getSymbolType() == null ? 3 : 0;
            int priceScale = depth.getSymbol().contains("BTC") || depth.getSymbol().contains("CNY") ? 2 : 3;

            Cell cell = new Cell(price.setScale(3, HALF_UP), map.get(price).setScale(volumeScale, HALF_UP),
                    BigDecimal.valueOf(open).setScale(volumeScale, HALF_UP));
            Cell c = cacheMap.get(id+index);

            if (c == null || !c.equals(cell)){
                BigDecimal trade = tradeMap.get(id+cell.price);

                handler.appendJavaScript("$('#" + id + " #price_" + index + "').text('" +
                        cell.price.setScale(priceScale, HALF_UP) + "')");
                handler.appendJavaScript("$('#" + id +" #volume_" + index + "').text('" +
                        cell.volume + "')");
                handler.appendJavaScript("$('#" + id + " #open_" + index + "').text('" +
                        (open > 0 ? cell.open : "") + "')");
                handler.appendJavaScript("$('#" + id + " #trade_" + index + "').text('" +
                        (trade != null ? trade.setScale(volumeScale, HALF_UP) : "") + "')");
            }

            cacheMap.put(id + index, cell);

            index++;
        }
    }

    protected ListView getListView(String id) {

        return new ListView<Integer>(id, IntStream.range(0, 40).boxed().collect(Collectors.toList())) {
            @SuppressWarnings("WicketForgeJavaIdInspection")
            @Override
            protected void populateItem(ListItem<Integer> item) {
                Integer index = item.getModelObject();

                String rowClass = index < 20 ? "ask" : "bid";

                item.add(new Label("price", Model.of("")).setMarkupId("price_" + index).add(ClassAttributeModifier.append("class", rowClass)));
                item.add(new Label("volume", Model.of("")).setMarkupId("volume_" + index).add(ClassAttributeModifier.append("class", rowClass)));
                item.add(new Label("open", Model.of("")).setMarkupId("open_" + index).add(ClassAttributeModifier.append("class", rowClass)));
                item.add(new Label("trade", Model.of("")).setMarkupId("trade_" + index).add(ClassAttributeModifier.append("class", rowClass)));
            }
        };
    }
}
