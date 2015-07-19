package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.UserInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author inheaven on 19.07.2015 16:22.
 */
public class UserInfoMapper extends BaseMapper<UserInfo>{
    public List<UserInfo> getUserInfoList(Long accountId, String currency, Date date){
        return sqlSession().selectList("selectUserInfoList", new HashMap<String, Object>(){{
            put("accountId", accountId);
            put("currency", currency);
            put("date", date);
        }});
    }

}
