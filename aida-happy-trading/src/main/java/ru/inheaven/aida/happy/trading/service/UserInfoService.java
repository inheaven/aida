package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.okcoin.dto.account.OkCoinFunds;
import com.xeiam.xchange.okcoin.dto.account.OkCoinFuturesUserInfoCross;
import com.xeiam.xchange.okcoin.dto.account.OkcoinFuturesFundsCross;
import com.xeiam.xchange.okcoin.service.polling.OkCoinAccountServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import ru.inheaven.aida.happy.trading.mapper.UserInfoMapper;
import ru.inheaven.aida.happy.trading.mapper.UserInfoTotalMapper;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN_CN;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;

/**
 * @author inheaven on 19.07.2015 17:15.
 */
@Singleton
public class UserInfoService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private XChangeService xChangeService;
    private UserInfoMapper userInfoMapper;
    private UserInfoTotalMapper userInfoTotalMapper;
    private BroadcastService broadcastService;
    private InfluxService influxService;

    private Subject<UserInfo, UserInfo> userInfoSubject = PublishSubject.create();

    private Map<String, BigDecimal> valueMap = new ConcurrentHashMap<>();


    @Inject
    public UserInfoService(AccountMapper accountMapper, XChangeService xChangeService, UserInfoMapper userInfoMapper,
                           UserInfoTotalMapper userInfoTotalMapper, TradeService tradeService, OrderService orderService,
                           BroadcastService broadcastService, InfluxService influxService) {
        this.xChangeService = xChangeService;
        this.userInfoMapper = userInfoMapper;
        this.userInfoTotalMapper = userInfoTotalMapper;
        this.broadcastService = broadcastService;
        this.influxService = influxService;

        accountMapper.getAccounts(OKCOIN_CN).forEach(this::startOkcoinUserInfoScheduler);

        tradeService.getTradeObservable()
                .subscribe(t -> {
                    try {
                        setPrice(t.getExchangeType(), t.getSymbol(), t.getSymbolType(), t.getPrice());
                    } catch (Exception e) {
                        log.error("error user info service update price", e);
                    }
                });

        //todo move to order service
        orderService.getClosedOrderObservable()
                .filter(o -> o.getStatus().equals(CLOSED))
                .subscribe(o -> {
                    try {
                        if (o.getSymbolType() != null) {
                            BigDecimal volume = getVolume("futures", o.getAccountId(), null);

                            if (o.getSymbol().equals("BTC/USD")) {
                                volume = volume.add(BigDecimal.valueOf(100).multiply(o.getAmount()));
                            } else if (o.getSymbol().equals("LTC/USD")) {
                                volume = volume.add(BigDecimal.valueOf(10).multiply(o.getAmount()));
                            }

                            setVolume("futures", o.getAccountId(), null, volume);
                        } else {
                            BigDecimal volume = getVolume("spot", o.getAccountId(), null);
                            setVolume("spot", o.getAccountId(), null, volume.add(o.getAmount().multiply(o.getPrice())));
                        }
                    } catch (Exception e) {
                        log.error("on close volume -> {}", o);
                    }
                });
    }

    private void setVolume(String key, Long accountId, String currency, BigDecimal volume){
        valueMap.put("volume" + key + accountId + currency, volume);
    }

    public BigDecimal getVolume(String key, Long accountId, String symbol){
        BigDecimal volume = valueMap.get("volume" + key + accountId + symbol);

        return volume != null ? volume : ZERO;
    }

    private void setPrice(ExchangeType exchangeType, String symbol, SymbolType symbolType, BigDecimal price){
        valueMap.put("price" + exchangeType.name() + symbol + symbolType, price);
    }

    public BigDecimal getPrice(ExchangeType exchangeType, String symbol, SymbolType symbolType){
        return valueMap.get("price" + exchangeType.name() + symbol + symbolType);
    }

    public Observable<UserInfo> createUserInfoObservable(Long accountId, String currency){
        return userInfoSubject.filter(u -> u.getAccountId().equals(accountId) && u.getCurrency().equals(currency));
    }

    private void startOkcoinUserInfoScheduler(Account account){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                OkCoinFunds funds = ((OkCoinAccountServiceRaw) xChangeService.getExchange(account)
                        .getPollingAccountService()).getUserInfo().getInfo().getFunds();
                saveFunds(account.getId(), "BTC_SPOT", funds.getFree().get("btc"), funds.getFreezed().get("btc"));
                saveFunds(account.getId(), "LTC_SPOT", funds.getFree().get("ltc"), funds.getFreezed().get("ltc"));
                saveFunds(account.getId(), "ASSET", funds.getAsset().get("total"), funds.getAsset().get("net"));

                switch (account.getExchangeType()){
                    case OKCOIN:
                        OkCoinFuturesUserInfoCross info = ((OkCoinAccountServiceRaw) xChangeService.getExchange(account)
                                .getPollingAccountService()).getFutureUserInfo();
                        saveFunds(account.getId(), "BTC", info.getInfo().getBtcFunds());
                        saveFunds(account.getId(), "LTC", info.getInfo().getLtcFunds());

                        saveTotal(account, funds.getAsset().get("net"), info.getInfo().getLtcFunds().getAccountRights(),
                                info.getInfo().getBtcFunds().getAccountRights());

                        saveFunds(account.getId(), "USD_SPOT", funds.getFree().get("usd"), funds.getFreezed().get("usd"));
                        break;
                    case OKCOIN_CN:
                        saveTotal(account, funds.getAsset().get("net"), null, null);

                        saveFunds(account.getId(), "CNY_SPOT", funds.getFree().get("cny"), funds.getFreezed().get("cny"));
                        break;
                }
            } catch (Exception e) {
                log.error("error user info -> ", e);
            }

        }, 0, 1, TimeUnit.MINUTES);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                OkCoinFunds funds = ((OkCoinAccountServiceRaw) xChangeService.getExchange(account)
                        .getPollingAccountService()).getUserInfo().getInfo().getFunds();

                setVolume("free", account.getId(), "BTC", funds.getFree().get("btc"));
                setVolume("free", account.getId(), "LTC", funds.getFree().get("ltc"));
                setVolume("subtotal", account.getId(), "BTC", funds.getFree().get("btc").add(funds.getFreezed().get("btc")));
                setVolume("subtotal", account.getId(), "LTC", funds.getFree().get("ltc").add(funds.getFreezed().get("ltc")));
                setVolume("total", account.getId(), null, funds.getAsset().get("total"));
                setVolume("net", account.getId(), null, funds.getAsset().get("net"));

                switch (account.getExchangeType()){
                    case OKCOIN:
                        setVolume("subtotal", account.getId(), "USD", funds.getFree().get("usd").add(funds.getFreezed().get("usd")));
                        setVolume("free", account.getId(), "CNY", funds.getFree().get("usd"));
                        break;
                    case OKCOIN_CN:
                        setVolume("subtotal", account.getId(), "CNY", funds.getFree().get("cny").add(funds.getFreezed().get("cny")));
                        setVolume("free", account.getId(), "CNY", funds.getFree().get("cny"));
                        break;
                }

                influxService.addAccountMetric(account.getId(),
                        getPrice(account.getExchangeType(), "BTC/CNY", null),
                        funds.getFree().get("btc"),
                        funds.getFree().get("btc").add(funds.getFreezed().get("btc")),
                        funds.getAsset().get("total"),
                        funds.getAsset().get("net"),
                        funds.getFreezed().get("cny"),
                        funds.getFreezed().get("btc"));

            } catch (Exception e) {
                log.error("error user info -> ", e);
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    private void saveFunds(Long accountId, String currency, BigDecimal free, BigDecimal freezed){
        UserInfo userInfo = new UserInfo();

        userInfo.setAccountId(accountId);
        userInfo.setCurrency(currency);
        userInfo.setAccountRights(free);
        userInfo.setKeepDeposit(freezed);
        userInfo.setProfitReal(ZERO);
        userInfo.setProfitUnreal(ZERO);
        userInfo.setRiskRate(ZERO);
        userInfo.setCreated(new Date());

        userInfoSubject.onNext(userInfo);
        broadcastService.broadcast(getClass(), "user_info", userInfo);

        userInfoMapper.save(userInfo);
    }

    private void saveFunds(Long accountId, String currency, OkcoinFuturesFundsCross funds){
        UserInfo userInfo = new UserInfo();

        userInfo.setAccountId(accountId);
        userInfo.setCurrency(currency);
        userInfo.setAccountRights(funds.getAccountRights());
        userInfo.setKeepDeposit(funds.getKeepDeposits());
        userInfo.setProfitReal(funds.getProfitReal());
        userInfo.setProfitUnreal(funds.getProfitUnreal());
        userInfo.setRiskRate(BigDecimal.valueOf(funds.getRiskRate()));
        userInfo.setCreated(new Date());

        userInfoSubject.onNext(userInfo);
        broadcastService.broadcast(getClass(), "user_info", userInfo);

        userInfoMapper.save(userInfo);
    }

    private void saveTotal(Account account, BigDecimal spotTotal, BigDecimal ltcAmount, BigDecimal btcAmount){
        UserInfoTotal userInfoTotal = new UserInfoTotal();

        String currency = account.getExchangeType().equals(OKCOIN_CN) ? "CNY" : "USD";

        BigDecimal ltcPrice = getPrice(account.getExchangeType(), "LTC/"+currency, null);
        BigDecimal btcPrice = getPrice(account.getExchangeType(), "BTC/"+currency, null);

        if (ltcPrice != null && btcPrice != null) {
            userInfoTotal.setAccountId(account.getId());
            userInfoTotal.setSpotTotal(spotTotal);

            if (btcAmount != null && ltcAmount != null) {
                userInfoTotal.setFuturesTotal(ltcAmount.multiply(ltcPrice).add(btcAmount.multiply(btcPrice)).setScale(8, HALF_UP));
            }else {
                userInfoTotal.setFuturesTotal(ZERO);
            }

            userInfoTotal.setFuturesVolume(getVolume("futures", account.getId(), null));
            userInfoTotal.setSpotVolume(getVolume("spot", account.getId(), null));
            userInfoTotal.setLtcPrice(ltcPrice);
            userInfoTotal.setBtcPrice(btcPrice);
            userInfoTotal.setCreated(new Date());

            setVolume("futures", account.getId(), null, ZERO);
            setVolume("spot", account.getId(), null, ZERO);

            userInfoTotalMapper.save(userInfoTotal);

            broadcastService.broadcast(UserInfoTotal.class, "user_info_total", userInfoTotal);
        }
    }
}
