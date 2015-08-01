package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.okcoin.dto.account.OkCoinFunds;
import com.xeiam.xchange.okcoin.dto.account.OkCoinFuturesUserInfoCross;
import com.xeiam.xchange.okcoin.dto.account.OkcoinFuturesFundsCross;
import com.xeiam.xchange.okcoin.service.polling.OkCoinAccountServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.UserInfo;
import ru.inheaven.aida.happy.trading.entity.UserInfoTotal;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN;
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

    private Subject<UserInfo, UserInfo> userInfoSubject = PublishSubject.create();

    private BigDecimal ltcPrice = ZERO;
    private BigDecimal btcPrice = ZERO;
    private BigDecimal spotVolume = ZERO;
    private BigDecimal futuresVolume = ZERO;

    @Inject
    public UserInfoService(AccountMapper accountMapper, XChangeService xChangeService, UserInfoMapper userInfoMapper,
                           UserInfoTotalMapper userInfoTotalMapper, TradeService tradeService, OrderService orderService,
                           BroadcastService broadcastService) {
        this.xChangeService = xChangeService;
        this.userInfoMapper = userInfoMapper;
        this.userInfoTotalMapper = userInfoTotalMapper;
        this.broadcastService = broadcastService;

        accountMapper.getAccounts(OKCOIN).forEach(this::startOkcoinUserInfoScheduler);

        tradeService.getTradeObservable()
                .filter(t -> t.getSymbolType() == null)
                .subscribe(t -> {
                    if (t.getSymbol().equals("BTC/USD")) {
                        btcPrice = t.getPrice();
                    } else if (t.getSymbol().equals("LTC/USD")) {
                        ltcPrice = t.getPrice();
                    }
                });

        orderService.getClosedOrderObservable()
                .filter(o -> o.getStatus().equals(CLOSED))
                .subscribe(o -> {
                    if (o.getSymbolType() != null) {
                        if (o.getSymbol().equals("BTC/USD")) {
                            futuresVolume = futuresVolume.add(BigDecimal.valueOf(100).multiply(o.getAmount()));
                        } else if (o.getSymbol().equals("LTC/USD")) {
                            futuresVolume = futuresVolume.add(BigDecimal.valueOf(10).multiply(o.getAmount()));
                        }
                    } else {
                        if (o.getSymbol().equals("BTC/USD")) {
                            spotVolume = spotVolume.add(o.getAmount().multiply(btcPrice));
                        } else if (o.getSymbol().equals("LTC/USD")) {
                            spotVolume = spotVolume.add(o.getAmount().multiply(ltcPrice));
                        }
                    }
                });
    }

    public Observable<UserInfo> createUserInfoObservable(Long accountId, String currency){
        return userInfoSubject.filter(u -> u.getAccountId().equals(accountId) && u.getCurrency().equals(currency));
    }

    private void startOkcoinUserInfoScheduler(Account account){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                OkCoinFuturesUserInfoCross info = ((OkCoinAccountServiceRaw) xChangeService.getExchange(account)
                        .getPollingAccountService()).getFutureUserInfo();
                saveFunds(account.getId(), "BTC", info.getInfo().getBtcFunds());
                saveFunds(account.getId(), "LTC", info.getInfo().getLtcFunds());

                OkCoinFunds funds = ((OkCoinAccountServiceRaw) xChangeService.getExchange(account)
                        .getPollingAccountService()).getUserInfo().getInfo().getFunds();
                saveFunds(account.getId(), "BTC_SPOT", funds.getFree().get("btc"), funds.getFreezed().get("btc"));
                saveFunds(account.getId(), "LTC_SPOT", funds.getFree().get("ltc"), funds.getFreezed().get("ltc"));
                saveFunds(account.getId(), "USD_SPOT", funds.getFree().get("usd"), funds.getFreezed().get("usd"));
                saveFunds(account.getId(), "ASSET", funds.getAsset().get("total"), funds.getAsset().get("net"));

                if (ltcPrice.compareTo(ZERO) > 0 && btcPrice.compareTo(ZERO) > 0){
                    saveTotal(account.getId(), funds.getAsset().get("total"), info.getInfo().getLtcFunds().getAccountRights(),
                            info.getInfo().getBtcFunds().getAccountRights());
                }
            } catch (Exception e) {
                log.error("error user info -> ", e);
            }

        }, 0, 1, TimeUnit.MINUTES);
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

    private void saveTotal(Long accountId, BigDecimal spotTotal, BigDecimal ltcAmount, BigDecimal btcAmount){
        UserInfoTotal userInfoTotal = new UserInfoTotal();

        userInfoTotal.setAccountId(accountId);
        userInfoTotal.setSpotTotal(spotTotal);
        userInfoTotal.setFuturesTotal(ltcAmount.multiply(ltcPrice).add(btcAmount.multiply(btcPrice)).setScale(8, HALF_UP));
        userInfoTotal.setFuturesVolume(futuresVolume);
        userInfoTotal.setSpotVolume(spotVolume);
        userInfoTotal.setLtcPrice(ltcPrice);
        userInfoTotal.setBtcPrice(btcPrice);
        userInfoTotal.setCreated(new Date());

        futuresVolume = ZERO;
        spotVolume = ZERO;

        broadcastService.broadcast(UserInfoTotal.class, "user_info_total", userInfoTotal);

        userInfoTotalMapper.save(userInfoTotal);
    }
}
