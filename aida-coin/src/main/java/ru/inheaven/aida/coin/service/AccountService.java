package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.util.TraderUtil;

import javax.ejb.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xeiam.xchange.dto.Order.OrderType.ASK;
import static java.math.BigDecimal.ZERO;
import static ru.inheaven.aida.coin.entity.ExchangeType.*;
import static ru.inheaven.aida.coin.service.ExchangeApi.getExchange;

/**
 * @author inheaven on 26.02.2015 23:49.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class AccountService extends AbstractService{
    private Logger log = LoggerFactory.getLogger(AccountService.class);

    @EJB
    private EntityBean entityBean;

    @EJB
    private TraderBean traderBean;

    @EJB
    private DataService dataService;

    @EJB
    private OrderService orderService;

    @EJB
    private StatService statService;

    private Map<ExchangeType, AccountInfo> accountInfoMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BalanceHistory> balanceHistoryMap = new ConcurrentHashMap<>();
    private Map<ExchangeType, Equity> equityMap = new ConcurrentHashMap<>();
    private Equity equity;


    //@Schedule(second = "*/42", minute="*", hour="*", persistent=false)
    public void scheduleAccounts(){
        for (ExchangeType exchangeType : ExchangeType.values()) {
            try {
                updateAccountInfo(exchangeType);
                updateEquity(exchangeType);
                updateEquity();
            } catch (Exception e) {
                log.error("scheduleAccounts", e);
            }
        }

        scheduleBalanceHistory();
    }

    public void scheduleBalanceHistory(){
        try {
            for (ExchangeType exchangeType : ExchangeType.values()){
                AccountInfo accountInfo = getAccountInfo(exchangeType);
                OpenOrders openOrders = orderService.getOpenOrders(exchangeType);

                if (accountInfo != null && openOrders != null){
                    //check ask amount
                    boolean zero = true;

                    for (LimitOrder limitOrder : openOrders.getOpenOrders()){
                        if (limitOrder.getType().equals(ASK)
                                && limitOrder.getLimitPrice().compareTo(BigDecimal.ZERO) != 0){
                            zero = false;
                            break;
                        }
                    }

                    if (zero){
                        continue;
                    }

                    List<Trader> traders = traderBean.getTraders(exchangeType);

                    for (Trader trader : traders){
                        Ticker ticker = dataService.getTicker(trader.getExchangePair());

                        if (ticker != null) {
                            CurrencyPair currencyPair = TraderUtil.getCurrencyPair(trader.getPair());

                            BigDecimal askAmount = ZERO;
                            BigDecimal bidAmount =  ZERO;

                            for (LimitOrder limitOrder : openOrders.getOpenOrders()){
                                if (currencyPair.equals(limitOrder.getCurrencyPair())){
                                    if (limitOrder.getType().equals(ASK)){
                                        askAmount = askAmount.add(limitOrder.getTradableAmount());
                                    }else{
                                        bidAmount = bidAmount.add(limitOrder.getTradableAmount());
                                    }
                                }
                            }

                            ExchangePair exchangePair = trader.getExchangePair();
                            BalanceHistory previous = balanceHistoryMap.get(exchangePair);

                            BalanceHistory h = new BalanceHistory();

                            h.setExchangeType(exchangeType);
                            h.setPair(trader.getPair());
                            h.setBalance(accountInfo.getBalance(trader.getCurrency()));
                            h.setAskAmount(askAmount);
                            h.setBidAmount(bidAmount);
                            h.setPrice(ticker.getLast());
                            h.setPrevious(previous);

                            if (previous != null &&  h.getPrice() != null){
                                boolean changed;

                                if (OKCOIN.equals(trader.getExchangeType())){
                                    double p1 = previous.getBalance().doubleValue();
                                    double p2 = h.getBalance().doubleValue();

                                    changed = Math.abs(p1 - p2) / p1 > 0.005;
                                }else{
                                    changed = !h.equals(previous);
                                }

                                if (changed) {
                                    try {
                                        entityBean.save(h);
                                    } catch (Exception e) {
                                        log.error("save balance history error", e);
                                    }
                                }

                                broadcast(exchangeType, h);
                            }

                            balanceHistoryMap.put(exchangePair, h);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("schedule balance history error", e);
        }
    }

    private void updateAccountInfo(ExchangeType exchangeType) {
        try {
            AccountInfo accountInfo = getExchange(exchangeType).getPollingAccountService().getAccountInfo();
            accountInfoMap.put(exchangeType, accountInfo);

            broadcast(exchangeType, accountInfo);
        } catch (IOException e) {
            log.error("updateAccountInfo error", e);
        }
    }


    public void updateEquity(ExchangeType exchangeType){
        AccountInfo accountInfo = getAccountInfo(exchangeType);

        if (accountInfo != null){
            BigDecimal volume = ZERO;

            for (Wallet wallet : accountInfo.getWallets()){
                volume = volume.add(statService.getEstimateBalance(exchangeType, wallet.getCurrency(), wallet.getBalance()));
            }

            if (BTCE.equals(exchangeType)){ //do check it
                for (LimitOrder limitOrder : orderService.getOpenOrders(ExchangeType.BTCE).getOpenOrders()){
                    volume = volume.add(statService.getBTCVolume(
                            ExchangePair.of(ExchangeType.BTCE, TraderUtil.getPair(limitOrder.getCurrencyPair())),
                            limitOrder.getTradableAmount(), limitOrder.getLimitPrice()));
                }
            }

            Equity equity = equityMap.get(exchangeType);

            if (equity == null || equity.getVolume().compareTo(volume) != 0){
                equity = new Equity(exchangeType, volume);

                equityMap.put(exchangeType, equity);

                entityBean.save(equity);

                broadcast(exchangeType, equity);
            }
        }
    }

    public void updateEquity(){
        BigDecimal volume = ZERO;

        for (ExchangeType exchangeType : ExchangeType.values()){
            Equity e = equityMap.get(exchangeType);

            if(e == null){
                if (!exchangeType.equals(BTER)) {
                    return;
                }
            }else{
                volume = volume.add(e.getVolume());
            }
        }

        equity = new Equity(volume);

        entityBean.save(equity);

        broadcast(null, equity);
    }

    public AccountInfo getAccountInfo(ExchangeType exchangeType){
        return accountInfoMap.get(exchangeType);
    }

    public BalanceHistory getBalanceHistory(ExchangePair exchangePair){ return balanceHistoryMap.get(exchangePair); }
}
