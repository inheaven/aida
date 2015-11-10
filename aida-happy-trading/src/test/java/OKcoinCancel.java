import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.okcoin.OkCoinExchange;

import java.io.IOException;

/**
 * @author inheaven on 15.09.2015 5:43.
 */
public class OKcoinCancel {

    public static void main(String... params) throws IOException {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class) {{
//            setApiKey("8b8620cf-83ed-46d8-91e6-41e5eb65f44f");
//            setSecretKey("DBB5E3FAA26238E9613BD73A3D4ECEDC");
//            setExchangeSpecificParametersItem("Use_Intl", false);

            setApiKey("832a335b-e627-49ca-b95d-bceafe6c3815");
            setSecretKey("8FAF74E300D67DCFA080A6425182C8B7");
            setExchangeSpecificParametersItem("Use_Intl", true);
        }});

        OpenOrders openOrders = exchange.getPollingTradeService().getOpenOrders();

        openOrders.getOpenOrders().forEach(o -> {
            try {
                System.out.println("cancel " + o);
                exchange.getPollingTradeService().cancelOrder(o.getId());

//                if (o.getLimitPrice().subtract(BigDecimal.valueOf(1616)).abs().compareTo(BigDecimal.valueOf(10)) > 0) {
//
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

}

