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
        super(ExchangeType.OKCOIN_CN, "8b8620cf-83ed-46d8-91e6-41e5eb65f44f", "DBB5E3FAA26238E9613BD73A3D4ECEDC",
                "client_market_cn.cfg",
                "client_trade_cn.cfg", "client_trade_cn_2.cfg", "client_trade_cn_3.cfg", "client_trade_cn_4.cfg",
                Arrays.asList("LTC/CNY", "BTC/CNY"));
    }
}

