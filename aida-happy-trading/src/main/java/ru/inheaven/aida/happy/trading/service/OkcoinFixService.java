package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;

import javax.inject.Singleton;
import java.util.Arrays;

/**
 * @author inheaven on 04.09.2015 2:46.
 */
@Singleton
public class OkcoinFixService extends BaseOkcoinFixService {
    public OkcoinFixService() {
        super(ExchangeType.OKCOIN, "832a335b-e627-49ca-b95d-bceafe6c3815", "8FAF74E300D67DCFA080A6425182C8B7",
                null, null, null, null, null, Arrays.asList("LTC/USD", "BTC/USD"));
    }
}
