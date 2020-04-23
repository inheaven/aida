package org.knowm.xchange.bittrex;

import org.knowm.xchange.bittrex.dto.BittrexBaseResponse;
import org.knowm.xchange.bittrex.dto.BittrexException;
import org.knowm.xchange.bittrex.dto.marketdata.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("v1.1")
@Produces(MediaType.APPLICATION_JSON)
public interface Bittrex {

  @GET
  @Path("/public/getticker")
  BittrexBaseResponse<BittrexTicker> getTicker(@QueryParam("market") String market)
      throws BittrexException, IOException;

  @GET
  @Path("public/getmarketsummary/")
  BittrexBaseResponse<List<BittrexMarketSummary>> getMarketSummary(
      @QueryParam("market") String market) throws BittrexException, IOException;

  @GET
  @Path("public/getmarketsummaries/")
  BittrexBaseResponse<List<BittrexMarketSummary>> getMarketSummaries()
      throws BittrexException, IOException;

  @GET
  @Path("public/getorderbook/")
  BittrexBaseResponse<BittrexDepth> getBook(
      @QueryParam("market") String market,
      @QueryParam("type") String type,
      @QueryParam("depth") int depth)
      throws BittrexException, IOException;

  @GET
  @Path("public/getmarkethistory/")
  BittrexBaseResponse<List<BittrexTrade>> getTrades(@QueryParam("market") String market)
      throws BittrexException, IOException;

  @GET
  @Path("public/getmarkets")
  BittrexBaseResponse<List<BittrexSymbol>> getSymbols() throws BittrexException, IOException;

  @GET
  @Path("public/getcurrencies")
  BittrexBaseResponse<List<BittrexCurrency>> getCurrencies() throws BittrexException, IOException;
}
