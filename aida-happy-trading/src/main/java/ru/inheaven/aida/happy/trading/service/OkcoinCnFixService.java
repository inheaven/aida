package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;

import javax.inject.Singleton;
import java.util.Arrays;

/**
 * @author inheaven on 04.09.2015 2:37.
 */
@Singleton
public class OkcoinCnFixService extends BaseOkcoinFixService {
    public OkcoinCnFixService() {
        super(ExchangeType.OKCOIN_CN, "3ead4fef-31c5-432c-b081-7f8f23713518", "2D62253699CA279C9CF807F38C91A2A7",
                "client_market_cn.cfg", null, null, null, null, Arrays.asList("LTC/CNY", "BTC/CNY"));
    }
}

