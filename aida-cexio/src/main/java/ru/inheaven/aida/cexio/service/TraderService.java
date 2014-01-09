package ru.inheaven.aida.cexio.service;

import ru.inheaven.aida.cexio.entity.Ticker;

import javax.ejb.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:06
 */
@Singleton
public class TraderService {
    public void getTicker(){
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://cex.io").path("api/ticker/GHS/BTC");

        Ticker ticker = target.request().get(Ticker.class);

        System.out.println(ticker);
    }
}
