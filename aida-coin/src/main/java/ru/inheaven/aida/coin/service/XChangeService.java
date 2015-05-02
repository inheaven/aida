package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.okcoin.FuturesContract;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.okcoin.OkCoinExchange;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeServiceRaw;
import ru.inheaven.aida.coin.entity.Account;
import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.Order;
import ru.inheaven.aida.coin.entity.OrderStatus;

import javax.ejb.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author inheaven on 03.05.2015 0:51.
 */
@Singleton
public class XChangeService {
    private Map<Account, Exchange> exchangeMap = new ConcurrentHashMap<>();

    public Exchange getExchange(Account account){
        Exchange exchange = exchangeMap.get(account);

        if (exchange == null){
            switch (account.getExchangeType()){
                case OKCOIN:
                    exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class));
                    break;
            }

            exchangeMap.put(account, exchange);
        }

        return exchange;
    }

    void placeLimitOrder(Order order) throws IOException {
        String orderId;

        if (order.getAccount().getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
            OkCoinTradeServiceRaw tradeService = (OkCoinTradeServiceRaw) getExchange(order.getAccount()).getPollingTradeService();

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

            orderId = String.valueOf(tradeService.futuresTrade(OkCoinAdapters.adaptSymbol(new CurrencyPair(order.getSymbol())),
                    String.valueOf(order.getType().getCode()), order.getPrice().toPlainString(), order.getAmount().toPlainString(),
                    futuresContract, 0, 10).getOrderId());
        }else{
            orderId = getExchange(order.getAccount()).getPollingTradeService()
                    .placeLimitOrder(new LimitOrder(com.xeiam.xchange.dto.Order.OrderType.valueOf(order.getType().name()),
                            order.getAmount(), new CurrencyPair(order.getSymbol()), null, null, order.getPrice()));
        }

        order.setOrderId(orderId);
        order.setStatus(OrderStatus.OPEN);
    }

    void cancelLimitOrder(Order order) throws IOException {
        getExchange(order.getAccount()).getPollingTradeService().cancelOrder(order.getOrderId());
        //todo okcoin future contract name
    }
}
