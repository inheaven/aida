package ru.inheaven.aida.coin.web;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.wicket.Component;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

import javax.ejb.EJB;
import java.math.BigDecimal;

/**
 * @author Anatoly Ivanov
 *         Date: 22.08.2014 20:05
 */
public class TraderMatrix extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    @EJB
    private TraderService traderService;

    private Table<ExchangePair, Integer, BigDecimal> orderTable = HashBasedTable.create();
    private Table<ExchangePair, Integer, Component> componentTable = HashBasedTable.create();

    public TraderMatrix() {



    }
}
