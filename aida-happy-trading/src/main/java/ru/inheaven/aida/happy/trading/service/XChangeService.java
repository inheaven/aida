package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.okcoin.FuturesContract;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.okcoin.OkCoinExchange;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinTradeResult;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;

import javax.inject.Singleton;
import java.util.Date;
import java.util.Objects;

/**
 * @author inheaven on 03.07.2015 22:20.
 */
@Singleton
public class XChangeService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private ThreadLocal<Exchange> okcoinExchangeThreadLocal = new ThreadLocal<>();

    public Exchange getExchange(Account account){
        switch (account.getExchangeType()){
            case OKCOIN_FUTURES:
            case OKCOIN_SPOT:
                Exchange exchange = okcoinExchangeThreadLocal.get();

                if (exchange == null){
                    exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class) {{
                        setApiKey(account.getApiKey());
                        setSecretKey(account.getSecretKey());
                        setExchangeSpecificParametersItem("Use_Intl", true);
                    }});

                    okcoinExchangeThreadLocal.set(exchange);

                    log.info("init exchange {}", exchange);
                }

                return exchange;
        }

        return null;
    }

    void placeLimitOrder(Account account, Order order) throws CreateOrderException {
        try {
            String orderId;

            if (order.getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
                OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(account).getPollingTradeService();

                FuturesContract futuresContract;
                switch (order.getSymbolType()){
                    case THIS_WEEK:
                        futuresContract = FuturesContract.ThisWeek;
                        break;
                    case NEXT_WEEK:
                        futuresContract = FuturesContract.NextWeek;
                        break;
                    case QUARTER:
                        futuresContract = FuturesContract.Quarter;
                        break;

                    default: throw new IllegalArgumentException();
                }

                OkCoinTradeResult result = tradeService.futuresTrade(OkCoinAdapters.adaptSymbol(new CurrencyPair(order.getSymbol())),
                        String.valueOf(order.getType().getCode()), order.getPrice().toPlainString(), order.getAmount().toPlainString(),
                        futuresContract, 0, 10);

                orderId = String.valueOf(result.getOrderId());
            }else{
                orderId = getExchange(account).getPollingTradeService()
                        .placeLimitOrder(new LimitOrder(com.xeiam.xchange.dto.Order.OrderType.valueOf(order.getType().name()),
                                order.getAmount(), new CurrencyPair(order.getSymbol()), null, null, order.getPrice()));
            }

            order.setOrderId(orderId);
            order.setStatus(OrderStatus.OPEN);
            order.setOpen(new Date());

            log.info("open order -> {} {} {} {}", order.getPrice(), order.getAmount(), order.getType(), Objects.toString(order.getSymbolType(), ""));
        } catch (Exception e) {
            log.error("error place limit order -> ", e);

            throw new CreateOrderException(e);
        }
    }
}
