package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bittrex.v1.BittrexExchange;
import com.xeiam.xchange.btce.v3.BTCEExchange;
import com.xeiam.xchange.bter.BTERExchange;
import com.xeiam.xchange.cexio.CexIOExchange;
import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.okcoin.OkCoinExchange;
import ru.inheaven.aida.coin.entity.ExchangeType;

import static com.xeiam.xchange.ExchangeFactory.INSTANCE;

/**
 * @author Anatoly Ivanov
 *         Date: 07.11.2014 9:13
 */
public class ExchangeApi {
    private final static boolean debug = false;

    private static Exchange bittrexExchange = INSTANCE.createExchange(new ExchangeSpecification(BittrexExchange.class){{
        if (debug) {
            setApiKey("52bb28e4e5454efd90b427e4ea1f4e1e");
            setSecretKey("6d4548bfbc404176829a94ae5471fd82");
        } else {
            setApiKey("14935ef36d8b4afc8204946be7ddd152");
            setSecretKey("44d84de3865e4fbfa4c17dd42c026d11");
        }
    }});

    private static Exchange cexIOExchange = INSTANCE.createExchange(new ExchangeSpecification(CexIOExchange.class){{
        setUserName("inheaven");

        if (debug) {
            setApiKey("HmmrODz0Jprink3Eoq8evh1U5MM");
            setSecretKey("ThF2ugrKLJTJCkv84F7gcfTHbo");
        } else {
            setApiKey("0rt9tOzQG2rGfZfGxsx1CtR9JA");
            setSecretKey("5ZpuaGOfpFdn96JisyCfR6wQvc");
        }
    }});

    private static Exchange cryptsyExchange = INSTANCE.createExchange(new ExchangeSpecification(CryptsyExchange.class){{
        setApiKey("d5569da15ae4d9b58c11220e424747289df76627");
        setSecretKey("f3a4732d55f7b9162ea6105187b36f9b1a88323c7f5bc8d1fcb2b8c98d3456384c31d1cf3ae443a6");
    }});

    private static Exchange btceExchange = INSTANCE.createExchange(new ExchangeSpecification(BTCEExchange.class){{
        if (debug) {
            setApiKey("14W4V394-QZ3TDV21-OGRMPLMN-2YK80MBP-NIJBL6QZ");
            setSecretKey("a55e7ab6b39917f4021c1ac5a322a4e6e0668130daf85878d7409704823fc198");
        } else {
            setApiKey("IR3KMDK9-JPP06NXH-GKGO2GPA-EC4BK5W0-L9QG482O");
            setSecretKey("05e3dbb59c2586df33c12e189382e18cb5de5af736a9a0897b6b23a1bca359b6");
        }
    }});

    private static Exchange bterExchange = INSTANCE.createExchange(new ExchangeSpecification(BTERExchange.class){{
        setApiKey("2DD5DEB3-720C-404C-95FE-84B52369F6E3");
        setSecretKey("0bf365f96b17f1828736df787c872796be51fe70a588062cc9630c3eedc144ad");
    }});

    private static Exchange bitfinexExchange = INSTANCE.createExchange(new ExchangeSpecification(BitfinexExchange.class){{
        if (debug) {
            setApiKey("RSvak6wVvlMI63Jmmpi7WvFlU1HmNr7fEIVzJnIefsH");
            setSecretKey("y8oITGvaIrqDgdkegK6i0QqmF7xRmImGERRuoKf4wSd");
        } else {
            setApiKey("mn6dQmAnpKPp3GZyN6Plxhmt5WdJwVVj6zFdIel6fRZ");
            setSecretKey("B8UxOTb6cdKwz7jDu1m1FMjFCxMiz82g21z78Z8tDeB");
        }
    }});

    private static Exchange okcoinExchange = INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class){{
        setApiKey("2017677");
        setSecretKey("F41C04C8917B62967D12030DA66DF202");
        setExchangeSpecificParametersItem("Use_Intl", true);
    }});

    public static Exchange getExchange(ExchangeType exchangeType){
        switch (exchangeType){
            case BITTREX:
                return bittrexExchange;
            case CEXIO:
                return cexIOExchange;
            case CRYPTSY:
                return cryptsyExchange;
            case BTCE:
                return btceExchange;
            case BTER:
                return bterExchange;
            case BITFINEX:
                return bitfinexExchange;
            case OKCOIN:
                return okcoinExchange;
        }

        throw new IllegalArgumentException();
    }
}
