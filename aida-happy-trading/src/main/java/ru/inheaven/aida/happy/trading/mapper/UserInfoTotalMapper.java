package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.UserInfoTotal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author inheaven on 30.07.2015 0:10.
 */
public class UserInfoTotalMapper extends BaseMapper<UserInfoTotal> {
    public List<UserInfoTotal> getUserInfoTotals(Long accountId, Date startDate){
        return sqlSession().selectList("selectUserInfoTotals", new HashMap<String, Object>(){{
            put("accountId", accountId);
            put("startDate", startDate);
        }});
    }
}
