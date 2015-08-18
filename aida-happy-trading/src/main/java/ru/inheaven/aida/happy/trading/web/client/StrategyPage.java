package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.web.BasePage;

/**
 * @author inheaven on 18.08.2015 18:30.
 */
public class StrategyPage extends BasePage{
    public StrategyPage() {
        add(new ListView<Strategy>("strategies", Module.getInjector().getInstance(StrategyMapper.class).getActiveStrategies()) {
            @Override
            protected void populateItem(ListItem<Strategy> item) {
                OrderMapper orderMapper = Module.getInjector().getInstance(OrderMapper.class);

                Strategy strategy = item.getModelObject();

                item.add(new Label("id", strategy.getId()));
                item.add(new Label("name", strategy.getName()));
                item.add(new Label("spread", strategy.getLevelSpread()));
                item.add(new Label("lot", strategy.getLevelLot()));
                item.add(new Label("min_trade_profit", orderMapper.getMinTradeProfit(strategy.getId(), null, null)));
                item.add(new Label("min_trade_volume", orderMapper.getMinTradeVolume(strategy.getId(), null, null)));
                item.add(new Label("random_trade_profit", orderMapper.getRandomTradeProfit(strategy.getId(), null, null)));
                item.add(new Label("random_trade_volume", orderMapper.getRandomTradeVolume(strategy.getId(), null, null)));
            }
        });

        OrderMapper orderMapper = Module.getInjector().getInstance(OrderMapper.class);

        add(new Label("min_trade_profit", orderMapper.getMinTradeProfit(null, null, null)));
        add(new Label("min_trade_volume", orderMapper.getMinTradeVolume(null, null, null)));
        add(new Label("random_trade_profit", orderMapper.getRandomTradeProfit(null, null, null)));
        add(new Label("random_trade_volume", orderMapper.getRandomTradeVolume(null, null, null)));
    }
}
