package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.okcoin.FuturesContract;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.okcoin.OkCoinExchange;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinFuturesOrderResult;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinOrderResult;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinTradeResult;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.exception.OrderInfoException;

import javax.inject.Singleton;
import java.util.Date;
import java.util.Objects;

import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN;

/**
 * @author inheaven on 03.07.2015 22:20.
 */
@Singleton
public class XChangeService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private ThreadLocal<Exchange> okcoinExchangeThreadLocal = new ThreadLocal<>();

    public Exchange getExchange(Account account){
        switch (account.getExchangeType()){
            case OKCOIN:
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

            if (account.getExchangeType().equals(OKCOIN)) {
                if (order.getSymbolType() != null){
                    OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(account).getPollingTradeService();

                    OkCoinTradeResult result = tradeService.futuresTrade(OkCoinAdapters.adaptSymbol(new CurrencyPair(order.getSymbol())),
                            String.valueOf(order.getType().getCode()), order.getPrice().toPlainString(), order.getAmount().toPlainString(),
                            getFuturesContract(order), 0, 10);

                    orderId = String.valueOf(result.getOrderId());
                }else{
                    orderId = getExchange(account).getPollingTradeService()
                            .placeLimitOrder(new LimitOrder(com.xeiam.xchange.dto.Order.OrderType.valueOf(order.getType().name()),
                                    order.getAmount(), new CurrencyPair(order.getSymbol()), null, null, order.getPrice()));
                }
            }else {
                throw new IllegalArgumentException("not yet implemented");
            }

            order.setOrderId(orderId);
            order.setStatus(OrderStatus.OPEN);
            order.setOpen(new Date());

            log.info("open order -> {} {} {} {} {}", order.getStrategyId(), order.getPrice().setScale(3, HALF_UP),
                    order.getAmount().setScale(3, HALF_UP), order.getType(), Objects.toString(order.getSymbolType(), ""));
        } catch (Exception e) {
            log.error("error place limit order -> ", e);

            throw new CreateOrderException(e);
        }
    }

    private FuturesContract getFuturesContract(Order order) {
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
        return futuresContract;
    }

    public void checkOrder(Account account, Order order) throws OrderInfoException {
        try {
            if (account.getExchangeType().equals(OKCOIN)) {
                OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(account).getPollingTradeService();

                int status;

                if (order.getSymbolType() != null){
                    OkCoinFuturesOrderResult result = tradeService.getFuturesOrder(Long.valueOf(order.getOrderId()),
                            OkCoinAdapters.adaptSymbol(new CurrencyPair(order.getSymbol())),
                            "1", "1", getFuturesContract(order));

                    status = result.getOrders().length == 1 ? result.getOrders()[0].getStatus() : -1;
                }else{
                    OkCoinOrderResult result = tradeService.getOrder(Long.parseLong(order.getOrderId()),
                            OkCoinAdapters.adaptSymbol(new CurrencyPair(order.getSymbol())));

                    status = result.getOrders().length == 1 ? result.getOrders()[0].getStatus() : -1;
                }

                switch (status) {
                    case -1:
                    case 4:
                        order.setStatus(OrderStatus.CANCELED);
                        break;
                    case 2:
                        order.setStatus(OrderStatus.CLOSED);
                        break;
                    default:
                        order.setStatus(OrderStatus.OPEN);
                        break;
                }
            }else {
                throw new IllegalArgumentException("not yet implemented");
            }
        } catch (Exception e) {
            throw new OrderInfoException(e);
        }
    }
}
