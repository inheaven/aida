package ru.inheaven.aida.happy.trading.rest;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.PathParam;
import org.wicketstuff.rest.contenthandling.json.objserialdeserial.JacksonObjectSerialDeserial;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import ru.inheaven.aida.happy.trading.mapper.UserInfoMapper;
import ru.inheaven.aida.happy.trading.service.Module;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 21.07.2015 20:27.
 */
@ResourcePath("/account_info_rest")
public class AccountInfoRest extends AbstractRestResource<JsonWebSerialDeserial>{
    private Long futureAccountId = 7L;
    private Long spotAccountId = 8L;
    private Date startDate = new Date(Timestamp.valueOf("2015-07-21 00:00:00").getTime());
    private Date startSpotDate = new Date(Timestamp.valueOf("2015-07-24 00:00:00").getTime());


    private UserInfoMapper userInfoMapper = Module.getInjector().getInstance(UserInfoMapper.class);

    public AccountInfoRest() {
        super(new JsonWebSerialDeserial(new JacksonObjectSerialDeserial()));
    }

    @MethodMapping("/equity/{currency}")
    public List getEquity(@PathParam("currency") String currency){
        List<List> array = new ArrayList<>();

        userInfoMapper.getUserInfoList(futureAccountId, currency,startDate)
                .forEach(i -> array.add(Arrays.asList(i.getCreated().getTime(), i.getAccountRights().setScale(3, HALF_UP))));

        return array;
    }

    @MethodMapping("/margin/{currency}")
    public List getMargin(@PathParam("currency") String currency){
        List<List> array = new ArrayList<>();

        userInfoMapper.getUserInfoList(futureAccountId, currency, startDate)
                .forEach(i -> array.add(Arrays.asList(i.getCreated().getTime(), i.getKeepDeposit().setScale(3, HALF_UP))));

        return array;
    }

    @MethodMapping("/profit/{currency}")
    public List getProfit(@PathParam("currency") String currency){
        List<List> array = new ArrayList<>();

        userInfoMapper.getUserInfoList(futureAccountId, currency, startDate)
                .forEach(i -> array.add(Arrays.asList(i.getCreated().getTime(), i.getProfitReal().setScale(3, HALF_UP))));

        return array;
    }

    @MethodMapping("/spot/{currency}")
    public List getSpot(@PathParam("currency") String currency){
        List<List> array = new ArrayList<>();

        userInfoMapper.getUserInfoList(spotAccountId, currency, startSpotDate)
                .forEach(i -> array.add(Arrays.asList(i.getCreated().getTime(), i.getAccountRights().add(i.getKeepDeposit())
                        .setScale(3, HALF_UP))));

        return array;
    }
}
