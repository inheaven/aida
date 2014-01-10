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
    private final static String API_SECRET = "2YJZlmVNrVKK9xBR6dkFplOuIfo";
    private final static String API_KEY = "jLXE67YpeJAAvOMdXcAGaMp2iys";
    private final static String USERNAME = "inheaven";

    private JsonApi<Ticker> TICKER_GHS_BTC = new JsonApi<>("http://inheaven.ru/cex.io.php?GHS_BTC", Ticker.class);
    private JsonApi<Ticker> TICKER_LTC_BTC = new JsonApi<>("http://inheaven.ru/cex.io.php?LTC_BTC", Ticker.class);
    private JsonApi<Ticker> TICKER_NMC_BTC = new JsonApi<>("http://inheaven.ru/cex.io.php?NMC_BTC", Ticker.class);
    private JsonApi<Ticker> TICKER_GHS_NMC = new JsonApi<>("http://inheaven.ru/cex.io.php?GHS_NMC", Ticker.class);

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
