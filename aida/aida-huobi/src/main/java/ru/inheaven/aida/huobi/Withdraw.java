package ru.inheaven.aida.huobi;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.huobi.HuobiExchange;
import org.knowm.xchange.huobi.service.HuobiDigest;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.RestProxyFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov
 * 24.09.2017 20:10
 */
public class Withdraw {
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    private interface Huobi{
        @SuppressWarnings("RestParamTypeInspection")
        @POST
        Map withdrawCoin(@FormParam("access_key") String accessKey, @FormParam("withdraw_amount") String withdrawAmount,
                         @FormParam("coin_type") int coinType, @FormParam("created") long created,
                         @FormParam("withdraw_address") String withdrawAddress, @FormParam("method") String method,
                         @FormParam("sign") ParamsDigest sing, @FormParam("trade_password") String tradePassword) throws IOException;
    }

    public static void main(String[] args) throws IOException {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(HuobiExchange.class.getName());

        ExchangeSpecification exchangeSpecification = exchange.getDefaultExchangeSpecification();
        exchangeSpecification.setApiKey("2abb0949-cbdb36aa-93e7d4e0-93660");
        exchangeSpecification.setSecretKey("91ca8d79-8e118c56-d7ebffe4-5f62a");

        exchange.applySpecification(exchangeSpecification);

        Huobi huobi = RestProxyFactory.createProxy(Huobi.class, exchange.getExchangeSpecification().getSslUri());
        String accessKey = exchange.getExchangeSpecification().getApiKey();
        HuobiDigest digest = new HuobiDigest(exchange.getExchangeSpecification().getSecretKey(), "secret_key");


        Map res = huobi.withdrawCoin(accessKey, "0.0748", 1, System.currentTimeMillis()/1000,
                "33mnC9USMCQKCrAym26D5KVoe3gX5jLQgt", "withdraw_coin", digest, "");

        System.out.println(res);
    }
}
