package com.xeiam.xchange.cexio.service.polling;

import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.NotAvailableFromExchangeException;
import com.xeiam.xchange.NotYetImplementedForExchangeException;
import com.xeiam.xchange.cexio.CexIOAdapters;
import com.xeiam.xchange.cexio.dto.trade.CexIOOrder;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.trade.*;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import com.xeiam.xchange.service.polling.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: brox
 * Since: 2/6/14
 */

public class CexIOTradeService extends CexIOTradeServiceRaw implements PollingTradeService {

  /**
   * Initialize common properties from the exchange specification
   * 
   * @param exchangeSpecification The {@link com.xeiam.xchange.ExchangeSpecification}
   */
  public CexIOTradeService(ExchangeSpecification exchangeSpecification) {

    super(exchangeSpecification);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {

    List<CexIOOrder> cexIOOrderList = getCexIOOpenOrders();

    return CexIOAdapters.adaptOpenOrders(cexIOOrderList);
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {

    throw new NotAvailableFromExchangeException();
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {

    CexIOOrder order = placeCexIOLimitOrder(limitOrder);

    return Long.toString(order.getId());
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {

    return cancelCexIOOrder(orderId);
  }

  @Override
  public UserTrades getTradeHistory(Object... objects) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    return null;
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams tradeHistoryParams) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    return null;
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return null;
  }

  @Override
  public Map<CurrencyPair, ? extends TradeMetaData> getTradeMetaDataMap() throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
    return null;
  }


}
