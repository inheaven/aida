package com.xeiam.xchange.bitfinex.v1;

import com.xeiam.xchange.bitfinex.v1.dto.BitfinexException;
import com.xeiam.xchange.bitfinex.v1.dto.account.BitfinexBalancesRequest;
import com.xeiam.xchange.bitfinex.v1.dto.account.BitfinexBalancesResponse;
import com.xeiam.xchange.bitfinex.v1.dto.account.BitfinexMarginInfosRequest;
import com.xeiam.xchange.bitfinex.v1.dto.account.BitfinexMarginInfosResponse;
import com.xeiam.xchange.bitfinex.v1.dto.trade.*;
import si.mazi.rescu.ParamsDigest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BitfinexAuthenticated extends Bitfinex {

  @POST
  @Path("order/new")
  BitfinexOrderStatusResponse newOrder(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexNewOrderRequest newOrderRequest) throws IOException, BitfinexException;

  @POST
  @Path("offer/new")
  BitfinexOfferStatusResponse newOffer(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexNewOfferRequest newOfferRequest) throws IOException, BitfinexException;

  @POST
  @Path("balances")
  BitfinexBalancesResponse[] balances(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexBalancesRequest balancesRequest) throws IOException, BitfinexException;

  @POST
  @Path("order/cancel")
  BitfinexOrderStatusResponse cancelOrders(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexCancelOrderRequest cancelOrderRequest) throws IOException, BitfinexException;

  @POST
  @Path("offer/cancel")
  BitfinexOfferStatusResponse cancelOffer(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexCancelOfferRequest cancelOfferRequest) throws IOException, BitfinexException;

  @POST
  @Path("orders")
  BitfinexOrderStatusResponse[] activeOrders(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexNonceOnlyRequest nonceOnlyRequest) throws IOException, BitfinexException;

  @POST
  @Path("offers")
  BitfinexOfferStatusResponse[] activeOffers(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexNonceOnlyRequest nonceOnlyRequest) throws IOException, BitfinexException;

  @POST
  @Path("positions")
  BitfinexActivePositionsResponse[] activePositions(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexNonceOnlyRequest nonceOnlyRequest) throws IOException, BitfinexException;

  @POST
  @Path("order/status")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BitfinexOrderStatusResponse orderStatus(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexOrderStatusRequest orderStatusRequest) throws IOException, BitfinexException;

  @POST
  @Path("offer/status")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BitfinexOfferStatusResponse offerStatus(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexOfferStatusRequest offerStatusRequest) throws IOException, BitfinexException;

  @POST
  @Path("mytrades")
  BitfinexTradeResponse[] pastTrades(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexPastTradesRequest pastTradesRequest) throws IOException, BitfinexException;

  @POST
  @Path("credits")
  BitfinexCreditResponse[] activeCredits(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexActiveCreditsRequest activeCreditsRequest) throws IOException,
      BitfinexException;

  @POST
  @Path("margin_infos")
  BitfinexMarginInfosResponse[] marginInfos(@HeaderParam("X-BFX-APIKEY") String apiKey, @HeaderParam("X-BFX-PAYLOAD") ParamsDigest payload,
      @HeaderParam("X-BFX-SIGNATURE") ParamsDigest signature, BitfinexMarginInfosRequest marginInfosRequest) throws IOException, BitfinexException;

}
