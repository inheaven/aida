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
        super(ExchangeType.OKCOIN_CN, "a8c57680-52bb-4285-bf65-70b1d42f12b0", "81E1188B3084682F497DEF3B3EF9F740",
                "client_market_cn.cfg", null, null, null, null, Arrays.asList("LTC/CNY", "BTC/CNY"));
    }
}

