package ru.inheaven.aida.happy.trading.rest;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.PathParam;
import org.wicketstuff.rest.contenthandling.json.objserialdeserial.JacksonObjectSerialDeserial;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import ru.inheaven.aida.happy.trading.entity.UserInfo;
import ru.inheaven.aida.happy.trading.mapper.UserInfoMapper;
import ru.inheaven.aida.happy.trading.mapper.UserInfoTotalMapper;
import ru.inheaven.aida.happy.trading.service.Module;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 21.07.2015 20:27.
 */
@ResourcePath("/account_info_rest")
public class AccountInfoRest extends AbstractRestResource<JsonWebSerialDeserial>{
    private Date startDate = new Date(Timestamp.valueOf("2015-07-21 00:00:00").getTime());
    private Date startCnDate = new Date(Timestamp.valueOf("2015-09-08 00:00:00").getTime());


    private UserInfoMapper userInfoMapper = Module.getInjector().getInstance(UserInfoMapper.class);
    private UserInfoTotalMapper userInfoTotalMapper = Module.getInjector().getInstance(UserInfoTotalMapper.class);

    public AccountInfoRest() {
        super(new JsonWebSerialDeserial(new JacksonObjectSerialDeserial()));
    }

    //todo array.map

    @MethodMapping("/user_info/{accountId}/{currency}")
    public List getUserInfo(@PathParam("accountId") Long accountId, @PathParam("currency") String currency){
        return userInfoMapper.getUserInfoList(accountId, currency, startDate).stream()
                .filter(i -> System.currentTimeMillis() - i.getCreated().getTime() < 1000*60*60*24*2 ||
                        i.getCreated().getTime()/60000 % 60 == 0)
                .map(i -> {
                    if (i.getCurrency().contains("SPOT")){
                        return Arrays.asList(i.getCreated().getTime(),
                                i.getAccountRights().add(i.getKeepDeposit()).setScale(3, HALF_UP));
                    }else {
                        return Arrays.asList(i.getCreated().getTime(), i.getAccountRights().setScale(3, HALF_UP));
                    }
                })
                .collect(Collectors.toList());
    }

    @MethodMapping("/user_info_total/{accountId}")
    public List getUserInfoTotal(@PathParam("accountId") Long accountId){
        return userInfoTotalMapper.getUserInfoTotals(accountId, accountId == 7 ? startDate: startCnDate).stream()
                .filter(i -> System.currentTimeMillis() - i.getCreated().getTime() < 1000*60*60*24*2 ||
                        i.getCreated().getTime()/60000 % 60 == 0)
                .map(t -> Arrays.asList(t.getCreated().getTime(),
                        t.getFuturesTotal().add(t.getSpotTotal()).setScale(3, HALF_UP),
                        t.getFuturesVolume().add(t.getSpotVolume()).setScale(3, HALF_UP),
                        t.getBtcPrice().setScale(3, HALF_UP),
                        t.getLtcPrice().setScale(3, HALF_UP)))
                .collect(Collectors.toList());
    }

    @MethodMapping("/3d/{accountId}")
    public List get3D(@PathParam("accountId") Long accountId){
        Date startDate = new Date(System.currentTimeMillis() - 1000*60*60*24*7);

        return userInfoMapper.getUserInfoList(accountId, null, startDate).stream()
                .filter(u -> u.getCurrency().contains("SPOT"))
                .filter(u -> u.getCreated().getTime()/60000 % 5 == 0)
                .collect(Collectors.groupingByConcurrent(UserInfo::getCreatedMinute))
                .entrySet()
                .stream()
                .map(e ->  {
                    Object[] v = new Object[4];

                    v[0] = e.getKey()*1000;

                    for (UserInfo u : e.getValue()){
                        switch (u.getCurrency()){
                            case "CNY_SPOT":
                            case "USD_SPOT":
                                v[1] = u.getAccountRights().add(u.getKeepDeposit()).setScale(2, HALF_UP);
                                break;
                            case "LTC_SPOT":
                                v[2] = u.getAccountRights().add(u.getKeepDeposit()).setScale(2, HALF_UP);
                                break;
                            case "BTC_SPOT":
                                v[3] = u.getAccountRights().add(u.getKeepDeposit()).setScale(2, HALF_UP);
                                break;
                        }

                    }

                    return v;
                })
                .filter(o -> o[0] != null && o[1] != null && o[2] != null && o[3] != null)
                .collect(Collectors.toList());

    }
}
