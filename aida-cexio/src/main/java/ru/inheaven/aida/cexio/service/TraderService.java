package ru.inheaven.aida.cexio.service;

import ru.inheaven.aida.cexio.entity.JsonApi;
import ru.inheaven.aida.cexio.entity.Ticker;

import javax.ejb.Singleton;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:06
 */
@Singleton
public class TraderService {
    private JsonApi<Ticker> TICKER_GHS_BTC = new JsonApi<>("https://cex.io/api/ticker/GHS/BTC", Ticker.class);
    private JsonApi<Ticker> TICKER_LTC_BTC = new JsonApi<>("https://cex.io/api/ticker/LTC/BTC", Ticker.class);
    private JsonApi<Ticker> TICKER_NMC_BTC = new JsonApi<>("https://cex.io/api/ticker/NMC/BTC", Ticker.class);
    private JsonApi<Ticker> TICKER_GHS_NMC = new JsonApi<>("https://cex.io/api/ticker/GHS/NMC", Ticker.class);

    public Ticker getTicker(String name){
        switch (name){
            case "GHS/BTC":
                return TICKER_GHS_BTC.get();
            case "LTC/BTC":
                return TICKER_LTC_BTC.get();
            case "NMC/BTC":
                return TICKER_NMC_BTC.get();
            case "GHS/NMC":
                return TICKER_GHS_NMC.get();

            default:
                return null;
        }
    }
}
