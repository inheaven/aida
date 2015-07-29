package ru.inheaven.aida.happy.trading.rest;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.PathParam;
import org.wicketstuff.rest.contenthandling.json.objserialdeserial.JacksonObjectSerialDeserial;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
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
    private Long accountId = 7L;
    private Date startDate = new Date(Timestamp.valueOf("2015-07-21 00:00:00").getTime());
    private Date startSpotDate = new Date(Timestamp.valueOf("2015-07-24 00:00:00").getTime());


    private UserInfoMapper userInfoMapper = Module.getInjector().getInstance(UserInfoMapper.class);
    private UserInfoTotalMapper userInfoTotalMapper = Module.getInjector().getInstance(UserInfoTotalMapper.class);

    public AccountInfoRest() {
        super(new JsonWebSerialDeserial(new JacksonObjectSerialDeserial()));
    }

    //todo array.map

    @MethodMapping("/user_info/{currency}")
    public List getUserInfo(@PathParam("currency") String currency){
        return userInfoMapper.getUserInfoList(accountId, currency,startDate).stream()
                .map(i -> Arrays.asList(i.getCreated().getTime(),
                        i.getAccountRights().setScale(3, HALF_UP),
                        i.getKeepDeposit().setScale(3, HALF_UP),
                        i.getProfitReal().setScale(3, HALF_UP)))
                .collect(Collectors.toList());
    }

    @MethodMapping("/spot/{currency}")
    public List getSpot(@PathParam("currency") String currency){
        return userInfoMapper.getUserInfoList(accountId, currency, startSpotDate).stream()
                .map(i -> Arrays.asList(i.getCreated().getTime(),
                        i.getAccountRights().add(i.getKeepDeposit()).setScale(3, HALF_UP)))
                .collect(Collectors.toList());
    }

    @MethodMapping("/user_info_total")
    public List getUserInfoTotal(){
        return userInfoTotalMapper.getUserInfoTotals(accountId, startDate).stream()
                .map(t -> Arrays.asList(t.getCreated().getTime(),
                        t.getFuturesTotal().add(t.getSpotTotal()).setScale(3, HALF_UP),
                        t.getFuturesVolume().add(t.getSpotVolume()).setScale(3, HALF_UP),
                        t.getBtcPrice().setScale(3, HALF_UP),
                        t.getLtcPrice().setScale(3, HALF_UP)))
                .collect(Collectors.toList());
    }
}
