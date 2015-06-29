package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Account;

import javax.inject.Inject;

/**
 * @author inheaven on 24.06.2015 20:05.
 */
public class AccountMapper extends BaseMapper<Account> {
    @Inject
    private StrategyMapper strategyMapper;

    private Account getAccount(Long id){
        return sqlSession().selectOne("selectAccount", id);
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
            s.setAccountId(account.getId());
            strategyMapper.save(s);
        });
    }
}
