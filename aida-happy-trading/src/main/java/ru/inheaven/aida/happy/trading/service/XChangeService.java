package ru.inheaven.aida.happy.trading.service;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.trade.LimitOrder;
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
import ru.inheaven.aida.happy.trading.util.OkcoinUtil;

import javax.inject.Singleton;
import java.util.Date;
import java.util.Objects;

import static com.xeiam.xchange.okcoin.OkCoinAdapters.adaptSymbol;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN_CN;

/**
 * @author inheaven on 03.07.2015 22:20.
 */
@Singleton
public class XChangeService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private ThreadLocal<Exchange> okcoinExchangeThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Exchange> okcoinCnExchangeThreadLocal = new ThreadLocal<>();

    public Exchange getExchange(Account account){
        Exchange exchange = null;

        switch (account.getExchangeType()){
            case OKCOIN:
                exchange = okcoinExchangeThreadLocal.get();
                if (exchange == null){
                    exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class) {{
                        setApiKey(account.getApiKey());
                        setSecretKey(account.getSecretKey());
                        setExchangeSpecificParametersItem("Use_Intl", true);
                    }});

                    okcoinExchangeThreadLocal.set(exchange);

                    log.info("init exchange {}", exchange);
                }
                break;
            case OKCOIN_CN:
                exchange = okcoinCnExchangeThreadLocal.get();
                if (exchange == null){
                    exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class) {{
                        setApiKey(account.getApiKey());
                        setSecretKey(account.getSecretKey());
                        setExchangeSpecificParametersItem("Use_Intl", false);
                    }});

                    okcoinCnExchangeThreadLocal.set(exchange);

                    log.info("init exchange {}", exchange);
                }
                break;
        }

        return exchange;
    }

    void placeLimitOrder(Account account, Order order) throws CreateOrderException {
        try {
            String orderId;

            if (account.getExchangeType().equals(OKCOIN) || account.getExchangeType().equals(OKCOIN_CN)) {
                if (order.getSymbolType() != null){
                    OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(account).getPollingTradeService();

                    OkCoinTradeResult result = tradeService.futuresTrade(adaptSymbol(new CurrencyPair(order.getSymbol())),
                            String.valueOf(order.getType().getCode()), order.getPrice().toPlainString(), order.getAmount().toPlainString(),
                            OkcoinUtil.getFuturesContract(order), 0, 10);

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



    public void checkOrder(Account account, Order order) throws OrderInfoException {
        try {
            if (account.getExchangeType().equals(OKCOIN) || account.getExchangeType().equals(OKCOIN_CN)) {
                OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(account).getPollingTradeService();

                int status;

                if (order.getSymbolType() != null){
                    OkCoinFuturesOrderResult result = tradeService.getFuturesOrder(Long.valueOf(order.getOrderId()),
                            adaptSymbol(new CurrencyPair(order.getSymbol())),
                            "1", "1", OkcoinUtil.getFuturesContract(order));

                    status = result.getOrders().length == 1 ? result.getOrders()[0].getStatus() : -1;
                }else{
                    OkCoinOrderResult result = tradeService.getOrder(Long.parseLong(order.getOrderId()),
                            adaptSymbol(new CurrencyPair(order.getSymbol())));

                    status = result.getOrders().length == 1 ? result.getOrders()[0].getStatus() : -1;
                }

                order.setStatus(OkcoinUtil.getOrderStatus(status));
            }else {
                throw new IllegalArgumentException("not yet implemented");
            }
        } catch (Exception e) {
            throw new OrderInfoException(e);
        }
    }

    public void cancelOrder(Account account, Order order) throws OrderInfoException {
        try {
            if (account.getExchangeType().equals(OKCOIN) || account.getExchangeType().equals(OKCOIN_CN)) {
                OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(account).getPollingTradeService();
                tradeService.cancelOrder(Long.parseLong(order.getOrderId()),  adaptSymbol(new CurrencyPair(order.getSymbol())));

                log.info("cancel order -> {} {} {} {} {}", order.getStrategyId(), order.getPrice().setScale(3, HALF_UP),
                        order.getAmount().setScale(3, HALF_UP), order.getType(), Objects.toString(order.getSymbolType(), ""));
            }else {
                throw new IllegalArgumentException("not yet implemented");
            }
        } catch (Exception e) {
            throw new OrderInfoException(e);
        }
    }
}
