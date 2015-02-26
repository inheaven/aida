package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.Trader;

import javax.ejb.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ZERO;
import static ru.inheaven.aida.coin.entity.ExchangeType.CRYPTSY;
import static ru.inheaven.aida.coin.entity.TraderType.LONG;
import static ru.inheaven.aida.coin.service.ExchangeApi.getExchange;
import static ru.inheaven.aida.coin.util.TraderUtil.getCurrencyPair;

/**
 * @author inheaven on 16.02.2015 22:55.
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class DataService extends AbstractService{
    private Logger log = LoggerFactory.getLogger(DataService.class);

    @EJB
    private EntityBean entityBean;

    @EJB
    private StatService statService;

    @EJB
    private TraderBean traderBean;

    private Map<ExchangePair, Ticker> tickerMap = new ConcurrentHashMap<>();

    public Ticker getTicker(ExchangePair exchangePair){
        return tickerMap.get(exchangePair);
    }

    private void updateTicker(ExchangeType exchangeType, boolean save) throws Exception {
        List<Trader> traders = traderBean.getTraders(exchangeType);

        for (Trader trader : traders) {
            try {
                CurrencyPair currencyPair = getCurrencyPair(trader.getPair());

                if (currencyPair != null && trader.getType().equals(LONG)) {
                    Ticker ticker;

                    if (CRYPTSY.equals(exchangeType)) {
                        ticker = ((CryptsyExchange)getExchange(exchangeType)).getPollingPublicMarketDataService().getTicker(currencyPair);
                    }else{
                        ticker = getExchange(exchangeType).getPollingMarketDataService().getTicker(currencyPair);
                    }

                    if (ticker.getLast() != null && ticker.getLast().compareTo(ZERO) != 0 && ticker.getBid() != null && ticker.getAsk() != null) {
                        ExchangePair ep = new ExchangePair(exchangeType, trader.getPair(), trader.getType());

                        Ticker previous = tickerMap.put(ep, ticker);

                        //ticker history
                        if (save) {
                            //todo move to stat service
//                            TickerHistory tickerHistory = new TickerHistory(exchangeType, trader.getPair(), ticker.getLast(),
//                                    ticker.getBid(), ticker.getAsk(), ticker.getVolume(),
//                                    getVolatilityIndex(ep), getPredictionIndex(ep));
//
//                            if (previous == null || previous.getLast().compareTo(ticker.getLast()) != 0){
//                                entityBean.save(tickerHistory);
//                            }
//
//                            if (previous == null || previous.getAsk().compareTo(ticker.getAsk()) != 0
//                                    || previous.getBid().compareTo(ticker.getBid()) != 0){
//                                broadcast(exchangeType, tickerHistory);
//                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error update ticker {} {}", exchangeType, trader.getPair());

                throw e;
            }
        }
    }



}
