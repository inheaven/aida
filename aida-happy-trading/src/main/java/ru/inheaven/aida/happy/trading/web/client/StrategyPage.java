package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import ru.inheaven.aida.happy.trading.entity.LevelParameter;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.web.BasePage;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 18.08.2015 18:30.
 */
public class StrategyPage extends BasePage{
    public StrategyPage() {
        add(new ListView<Strategy>("strategies", Module.getInjector().getInstance(StrategyMapper.class).getStrategies()) {
            @Override
            protected void populateItem(ListItem<Strategy> item) {
                OrderMapper orderMapper = Module.getInjector().getInstance(OrderMapper.class);

                Strategy strategy = item.getModelObject();

                item.add(new Label("id", strategy.getId()));
                item.add(new Label("name", strategy.getName()));
                item.add(new Label("spread", strategy.getBigDecimal(LevelParameter.SPREAD)));
                item.add(new Label("lot", strategy.getBigDecimal(LevelParameter.LOT)));
                item.add(new Label("active", strategy.isActive()));
                item.add(new Label("min_trade_profit", orderMapper.getMinTradeProfit(null, strategy.getId(), null, null)));
                item.add(new Label("min_trade_volume", orderMapper.getMinTradeVolume(null, strategy.getId(), null, null)));
                item.add(new Label("random_trade_profit", strategy.getSymbolType() == null ? orderMapper.getRandomTradeProfit(null, strategy.getId(), null, null) : "—"));
                item.add(new Label("random_trade_volume", strategy.getSymbolType() == null ? orderMapper.getRandomTradeVolume(null, strategy.getId(), null, null) : "—"));
                item.add(new Label("count", orderMapper.getTradeCount(null, strategy.getId(), null, null)));
            }
        });

        OrderMapper orderMapper = Module.getInjector().getInstance(OrderMapper.class);

        add(new Label("min_trade_profit", orderMapper.getMinTradeProfit(7L, null, null, null).setScale(2, HALF_UP) + " " +
                orderMapper.getMinTradeProfit(8L, null, null, null).setScale(2, HALF_UP)));
        add(new Label("min_trade_volume", orderMapper.getMinTradeVolume(7L, null, null, null).setScale(2, HALF_UP) + " " +
                orderMapper.getMinTradeVolume(8L, null, null, null).setScale(2, HALF_UP)));
        add(new Label("random_trade_profit", orderMapper.getRandomTradeProfit(7L, null, null, null).setScale(2, HALF_UP) + " " +
                orderMapper.getRandomTradeProfit(8L, null, null, null).setScale(2, HALF_UP)));
        add(new Label("random_trade_volume", orderMapper.getRandomTradeVolume(7L, null, null, null).setScale(2, HALF_UP) + " " +
                orderMapper.getRandomTradeVolume(8L, null, null, null).setScale(2, HALF_UP)));
    }
}
