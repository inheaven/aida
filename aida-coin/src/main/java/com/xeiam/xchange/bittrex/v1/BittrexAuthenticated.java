package com.xeiam.xchange.bittrex.v1;

import com.xeiam.xchange.bittrex.v1.dto.account.BittrexBalancesResponse;
import com.xeiam.xchange.bittrex.v1.dto.account.BittrexDepositAddressResponse;
import com.xeiam.xchange.bittrex.v1.dto.trade.*;
import si.mazi.rescu.ParamsDigest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("v1.1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BittrexAuthenticated extends Bittrex {

  @GET
  @Path("account/getdepositaddress")
  BittrexDepositAddressResponse getdepositaddress(@QueryParam("apikey") String apiKey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce,
      @QueryParam("currency") String currency) throws IOException;

  @GET
  @Path("account/getbalances")
  BittrexBalancesResponse balances(@QueryParam("apikey") String apiKey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce) throws IOException;

  @GET
  @Path("market/buylimit")
  BittrexTradeResponse buylimit(@QueryParam("apikey") String apikey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce, @QueryParam("market") String market,
      @QueryParam("quantity") String quantity, @QueryParam("rate") String rate) throws IOException;

  @GET
  @Path("market/selllimit")
  BittrexTradeResponse selllimit(@QueryParam("apikey") String apikey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce, @QueryParam("market") String market,
      @QueryParam("quantity") String quantity, @QueryParam("rate") String rate) throws IOException;

  @GET
  @Path("market/buymarket")
  BittrexTradeResponse buymarket(@QueryParam("apikey") String apikey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce, @QueryParam("market") String market,
      @QueryParam("quantity") String quantity) throws IOException;

  @GET
  @Path("market/sellmarket")
  BittrexTradeResponse sellmarket(@QueryParam("apikey") String apikey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce, @QueryParam("market") String market,
      @QueryParam("quantity") String quantity) throws IOException;

  @GET
  @Path("market/cancel")
  BittrexCancelOrderResponse cancel(@QueryParam("apikey") String apiKey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce, @QueryParam("uuid") String uuid)
      throws IOException;

  @GET
  @Path("market/getopenorders")
  BittrexOpenOrdersResponse openorders(@QueryParam("apikey") String apiKey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce) throws IOException;

  @GET
  @Path("account/getorder")
  BittrexOrderResponse getorder(@QueryParam("apikey") String apiKey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce, @QueryParam("uuid") String uuid) throws IOException;


  @GET
  @Path("account/getorderhistory")
  BittrexTradeHistoryResponse getorderhistory(@QueryParam("apikey") String apiKey, @HeaderParam("apisign") ParamsDigest signature, @QueryParam("nonce") String nonce) throws IOException;
}