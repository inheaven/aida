package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.okcoin.dto.account.OkCoinFuturesUserInfoCross;
import com.xeiam.xchange.okcoin.dto.account.OkcoinFuturesFundsCross;
import com.xeiam.xchange.okcoin.service.polling.OkCoinAccountServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.UserInfo;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import ru.inheaven.aida.happy.trading.mapper.UserInfoMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author inheaven on 19.07.2015 17:15.
 */
@Singleton
public class UserInfoService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private XChangeService xChangeService;
    private UserInfoMapper userInfoMapper;
    private BroadcastService broadcastService;

    @Inject
    public UserInfoService(AccountMapper accountMapper, XChangeService xChangeService, UserInfoMapper userInfoMapper,
                           BroadcastService broadcastService) {
        this.xChangeService = xChangeService;
        this.userInfoMapper = userInfoMapper;
        this.broadcastService = broadcastService;

        accountMapper.getAccounts(ExchangeType.OKCOIN_FUTURES).forEach(this::startUserInfoScheduler);
    }

    private void startUserInfoScheduler(Account account){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                OkCoinFuturesUserInfoCross info = ((OkCoinAccountServiceRaw) xChangeService.getExchange(account)
                        .getPollingAccountService()).getFutureUserInfo();

                saveFunds(account.getId(), "BTC", info.getInfo().getBtcFunds());
                saveFunds(account.getId(), "LTC", info.getInfo().getLtcFunds());
            } catch (Exception e) {
                log.error("error user info -> ", e);
            }

        }, 0, 1, TimeUnit.MINUTES);
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

        userInfoMapper.save(userInfo);

        broadcastService.broadcast(getClass(), "user_info", userInfo);
    }
}
