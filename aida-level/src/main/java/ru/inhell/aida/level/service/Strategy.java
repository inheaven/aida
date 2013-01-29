package ru.inhell.aida.level.service;

import ru.inhell.aida.common.entity.Bar;
import ru.inhell.aida.common.entity.Order;
import ru.inhell.aida.level.entity.Level;
import ru.inhell.aida.level.entity.Stock;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.01.13 5:32
 */
public class Strategy {
    private Stock stock;

    public Strategy() {

    }

    public void init(String symbol, int lot, float minPrice, int levels, float buyDelta, float sellDelta){
        stock = new Stock(symbol, lot);

        for (int i=0; i < levels; ++i){
            float buyPrice = minPrice + i*buyDelta;
            float sellPrice = buyPrice + sellDelta;

            stock.getLevels().add(new Level(i, lot, buyPrice, sellPrice));
        }
    }

    public void onBar(Bar bar){

    }

    public void onOrder(Order order){

    }




}
