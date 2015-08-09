package ru.inheaven.aida.happy.trading.rest;

import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.ResourcePath;
import org.wicketstuff.rest.annotations.parameters.PathParam;
import org.wicketstuff.rest.contenthandling.json.objserialdeserial.JacksonObjectSerialDeserial;
import org.wicketstuff.rest.contenthandling.json.webserialdeserial.JsonWebSerialDeserial;
import org.wicketstuff.rest.resource.AbstractRestResource;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.mapper.DepthMapper;
import ru.inheaven.aida.happy.trading.service.Module;

import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author inheaven on 07.08.2015 1:27.
 */
@ResourcePath("/arbitrage_rest")
public class ArbitrageRest extends AbstractRestResource<JsonWebSerialDeserial> {
    private Date startDate = new Date(Timestamp.valueOf("2015-08-06 00:00:00").getTime());

    private DepthMapper depthMapper = Module.getInjector().getInstance(DepthMapper.class);

    public ArbitrageRest() {
        super(new JsonWebSerialDeserial(new JacksonObjectSerialDeserial()));
    }

    @MethodMapping("/spreads/{currency}")
    public List getSpreads(@PathParam("currency") String currency){
        return depthMapper.getDepths(ExchangeType.OKCOIN, currency + "/USD", null, startDate).stream()
                .map(d -> Arrays.asList(d.getCreated(), d.getBid().setScale(3, RoundingMode.HALF_UP),
                        d.getAsk().setScale(3, RoundingMode.HALF_UP),
                        d.getSymbolType() != null ? d.getSymbolType().ordinal() : -1))
                .collect(Collectors.toList());
    }

}
