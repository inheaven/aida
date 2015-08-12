package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.SymbolType;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.StrategyService;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.json.Json;
import javax.json.JsonArray;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 12.08.2015 21:12.
 */
public class DepthPage extends BasePage{
    public DepthPage() {
        setVersioned(false);

        add(getListView("ltc_depth"));
        add(getListView("ltc_this_depth"));
        add(getListView("ltc_next_depth"));
        add(getListView("btc_depth"));
        add(getListView("btc_this_depth"));
        add(getListView("btc_next_depth"));

        add(new BroadcastBehavior<Depth>(DepthService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Depth depth) {
                String id = null;

                if (depth.getSymbol().equals("LTC/USD")) {
                    if (depth.getSymbolType() == null) {
                        id = "ltc_depth";
                    }else if (depth.getSymbolType().equals(SymbolType.THIS_WEEK)){
                        id = "ltc_this_depth";
                    }else if (depth.getSymbolType().equals(SymbolType.NEXT_WEEK)){
                        id = "ltc_next_depth";
                    }
                }else if (depth.getSymbol().equals("BTC/USD")){
                    if (depth.getSymbolType() == null) {
                        id = "btc_depth";
                    }else if (depth.getSymbolType().equals(SymbolType.THIS_WEEK)){
                        id = "btc_this_depth";
                    }else if (depth.getSymbolType().equals(SymbolType.NEXT_WEEK)){
                        id = "btc_next_depth";
                    }
                }

                if (id != null){
                    update(id, handler, depth, Module.getInjector().getInstance(StrategyService.class)
                            .getStrategies(depth.getExchangeType(), depth.getSymbol(), depth.getSymbolType()));
                }
            }
        });
    }

    private class Cell{
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

    private Map<String, Cell> cache = new HashMap<>();

    private void update(String key, WebSocketRequestHandler handler, Depth depth, List<BaseStrategy> ltcSpot) {
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
            double open = ltcSpot.stream()
                    .map(BaseStrategy::getOrderMap)
                    .flatMap(m -> m.values().stream())
                    .filter(o -> o.getPrice().setScale(3, HALF_UP).equals(price.setScale(3, HALF_UP)))
                    .collect(Collectors.summingDouble(o -> o.getAmount().doubleValue()));

            Cell cell = new Cell(price.setScale(3, HALF_UP), map.get(price).setScale(3, HALF_UP), BigDecimal.valueOf(open).setScale(3, HALF_UP));
            Cell c = cache.get(key+index);

            if (c == null || !c.equals(cell)){
                handler.appendJavaScript("$('#" + key + " #price_" + index + "').text('" + cell.price + "')");
                handler.appendJavaScript("$('#" + key +" #volume_" + index + "').text('" + cell.volume + "')");
                handler.appendJavaScript("$('#" + key + " #open_" + index + "').text('" +
                        (open > 0 ? cell.open : "") + "')");
            }

            cache.put(key+index, cell);

            index++;
        }
    }

    protected ListView getListView(String id) {
        ListView listView = new ListView<Integer>(id, IntStream.range(0, 40).boxed().collect(Collectors.toList())) {
            @SuppressWarnings("WicketForgeJavaIdInspection")
            @Override
            protected void populateItem(ListItem<Integer> item) {
                Integer index = item.getModelObject();

                String rowClass = index < 20 ? "ask" : "bid";

                item.add(new Label("price", Model.of("")).setMarkupId("price_" + index).add(ClassAttributeModifier.append("class", rowClass)));
                item.add(new Label("volume", Model.of("")).setMarkupId("volume_" + index).add(ClassAttributeModifier.append("class", rowClass)));
                item.add(new Label("open", Model.of("")).setMarkupId("open_" + index).add(ClassAttributeModifier.append("class", rowClass)));
            }
        };

        return listView;
    }
}
