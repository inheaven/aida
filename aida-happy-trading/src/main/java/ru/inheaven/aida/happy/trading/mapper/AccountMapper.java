package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;

import javax.inject.Inject;
import java.util.List;

/**
 * @author inheaven on 24.06.2015 20:05.
 */
public class AccountMapper extends BaseMapper<Account> {
    @Inject
    private StrategyMapper strategyMapper;

    public Account getAccount(Long id){
        return sqlSession().selectOne("selectAccount", id);
    }

    public List<Account> getAccounts(ExchangeType exchangeType){
        return sqlSession().selectList("selectAccountsByExchange", exchangeType);
    }

    @Override
    public void save(Account account) {
        super.save(account);

        getAccount(account.getId())
                .getStrategies()
                .forEach(s -> {
                            if (!account.getStrategies().stream()
                                    .filter(s1 -> s.getId().equals(s1.getId()))
                                    .findAny()
                                    .isPresent()) {
                                strategyMapper.delete(s);
                            }
                        }
                );

        account.getStrategies().forEach(s -> {
            s.setAccount(account);
            strategyMapper.save(s);
        });
    }
}
