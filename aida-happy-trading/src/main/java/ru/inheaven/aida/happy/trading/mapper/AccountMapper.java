package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;

import java.util.List;

/**
 * @author inheaven on 24.06.2015 20:05.
 */
public class AccountMapper extends BaseMapper<Account> {
    public Account getAccount(Long id){
        return sqlSession().selectOne("selectAccount", id);
    }

    public List<Account> getAccounts(ExchangeType exchangeType){
        return sqlSession().selectList("selectAccountsByExchange", exchangeType);
    }
}
