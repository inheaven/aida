package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.okcoin.dto.trade.OkCoinPosition;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinPositionResult;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.FuturesPosition;
import ru.inheaven.aida.happy.trading.entity.SymbolType;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import ru.inheaven.aida.happy.trading.mapper.FuturesPositionMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN;
import static ru.inheaven.aida.happy.trading.util.OkcoinUtil.toContract;
import static ru.inheaven.aida.happy.trading.util.OkcoinUtil.toSymbol;

/**
 * @author inheaven on 01.09.2015 2:12.
 */
@Singleton
public class FuturesPositionService {
    private Logger log = LoggerFactory.getLogger(FuturesPositionService.class);

    private XChangeService xChangeService;
    private FuturesPositionMapper futuresPositionMapper;

    private Map<String, FuturesPosition> futuresPositionMap = new HashMap<>();

    @Inject
    public FuturesPositionService(AccountMapper accountMapper,  XChangeService xChangeService,
                                  FuturesPositionMapper futuresPositionMapper) {
        this.xChangeService = xChangeService;
        this.futuresPositionMapper = futuresPositionMapper;

        accountMapper.getAccounts(OKCOIN).forEach(this::startFuturePositionScheduler);
    }

    private void startFuturePositionScheduler(Account account){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
            saveOkcoinFuturesPosition(account, "LTC/USD", SymbolType.THIS_WEEK);
            saveOkcoinFuturesPosition(account, "LTC/USD", SymbolType.NEXT_WEEK);
            saveOkcoinFuturesPosition(account, "LTC/USD", SymbolType.QUARTER);

            saveOkcoinFuturesPosition(account, "BTC/USD", SymbolType.THIS_WEEK);
            saveOkcoinFuturesPosition(account, "BTC/USD", SymbolType.NEXT_WEEK);
            saveOkcoinFuturesPosition(account, "BTC/USD", SymbolType.QUARTER);
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void saveOkcoinFuturesPosition(Account account, String symbol, SymbolType symbolType){
        try {
            OkCoinPositionResult result = ((OkCoinTradeServiceRaw) xChangeService.getExchange(account)
                    .getPollingTradeService()).getFuturesPosition(toSymbol(symbol), toContract(symbolType));

            if (result.getPositions().length > 0){
                OkCoinPosition p = result.getPositions()[0];

                FuturesPosition futuresPosition = new FuturesPosition();

                futuresPosition.setAccountId(account.getId());
                futuresPosition.setSymbol(symbol);
                futuresPosition.setSymbolType(symbolType);

                futuresPosition.setBuyAmount(p.getBuyAmount());
                futuresPosition.setBuyAvailable(p.getBuyAmountAvailable());
                futuresPosition.setBuyPriceAvg(p.getBuyPriceAvg());
                futuresPosition.setBuyPriceCost(p.getBuyPriceCost());
                futuresPosition.setBuyProfitReal(p.getBuyProfitReal());

                futuresPosition.setSellAmount(p.getSellAmount());
                futuresPosition.setSellAvailable(p.getSellAmountAvailable());
                futuresPosition.setSellPriceAvg(p.getSellPriceAvg());
                futuresPosition.setSellPriceCost(p.getSellPriceCost());
                futuresPosition.setSellProfitReal(p.getSellProfitReal());

                futuresPosition.setLeverRate(p.getRate());
                futuresPosition.setForceLiquPrice(result.getForceLiquPrice());
                futuresPosition.setContractId(p.getContractId());
                futuresPosition.setContractDate(p.getCreateDate());

                String k = account.getId() + symbol + symbolType;
                FuturesPosition f = futuresPositionMap.get(k);

                if (f == null || !f.equals(futuresPosition)){
                    futuresPositionMapper.save(futuresPosition);

                    futuresPositionMap.put(k, futuresPosition);
                }
            }
        } catch (Exception e) {
            log.error("error futures position -> ", e);
        }
    }


}
