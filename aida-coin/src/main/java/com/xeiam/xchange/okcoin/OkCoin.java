package com.xeiam.xchange.okcoin;

import com.xeiam.xchange.okcoin.dto.account.OkCoinUserInfo;
import com.xeiam.xchange.okcoin.dto.marketdata.OkCoinDepth;
import com.xeiam.xchange.okcoin.dto.marketdata.OkCoinTickerResponse;
import com.xeiam.xchange.okcoin.dto.marketdata.OkCoinTrade;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinOrderResult;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinPositionResult;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinTradeResult;
import si.mazi.rescu.ParamsDigest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/v1")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface OkCoin {

	@GET
	@Path("ticker.do")
	OkCoinTickerResponse getTicker(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol) throws IOException;

	@GET
	@Path("future_ticker.do")
	OkCoinTickerResponse getTicker(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol, @QueryParam("contract_type") String prompt)
			throws IOException;

	@GET
	@Path("depth.do")
	OkCoinDepth getDepth(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol) throws IOException;

	@GET
	@Path("future_depth.do")
	OkCoinDepth getDepth(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol, @QueryParam("contract_type") String prompt) throws IOException;

	@GET
	@Path("trades.do")
	OkCoinTrade[] getTrades(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol) throws IOException;

	@GET
	@Path("future_trades.do")
	OkCoinTrade[] getTrades(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol, @QueryParam("contract_type") String prompt) throws IOException;

	@GET
	@Path("trades.do")
	OkCoinTrade[] getTrades(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol, @QueryParam("since") long since) throws IOException;

	@GET
	@Path("future_trades.do")
	OkCoinTrade[] getTrades(@QueryParam("ok") String ok, @QueryParam("symbol") String symbol, @QueryParam("contract_type") String prompt,
			@QueryParam("since") long since) throws IOException;

	@POST
	@Path("userinfo.do")
	OkCoinUserInfo getUserInfo(@FormParam("partner") long partner, @FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("future_userinfo.do")
	OkCoinUserInfo getUserFutuersInfo(@FormParam("partner") long partner, @FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("trade.do")
	OkCoinTradeResult trade(@FormParam("partner") long partner, @FormParam("symbol") String symbol, @FormParam("type") String type,
			@FormParam("rate") String rate, @FormParam("amount") String amount, @FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("future_trade.do")
	OkCoinTradeResult trade(@FormParam("partner") long partner, @FormParam("symbol") String symbol, @FormParam("contract_type") String prompt,
			@FormParam("type") String type, @FormParam("price") String price, @FormParam("amount") String amount, @FormParam("sign") ParamsDigest sign,
			@FormParam("match_price") long matchPrice) throws IOException;

	@POST
	@Path("cancelorder.do")
	OkCoinTradeResult cancelOrder(@FormParam("partner") long partner, @FormParam("order_id") long orderId, @FormParam("symbol") String symbol,
			@FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("future_cancel.do")
	OkCoinTradeResult cancelOrder(@FormParam("partner") long partner, @FormParam("order_id") long orderId, @FormParam("symbol") String symbol,
			@FormParam("contract_type") String prompt, @FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("order_info.do")
	OkCoinOrderResult getOrder(@FormParam("partner") long partner, @FormParam("order_id") long orderId, @FormParam("symbol") String symbol,
			@FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("future_order_info.do")
	OkCoinOrderResult getOrder(@FormParam("partner") long partner, @FormParam("order_id") long orderId, @FormParam("symbol") String symbol,
			@FormParam("contract_type") String prompt,
            @FormParam("status") String status, @FormParam("current_page") Integer currentPage, @FormParam("page_length") Integer pageLength,
            @FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("future_position_4fix.do")
	OkCoinPositionResult getFuturesPositions(@FormParam("partner") long partner, @FormParam("symbol") String symbol, @FormParam("contract_type") String prompt,
			@FormParam("sign") ParamsDigest sign) throws IOException;

	@POST
	@Path("getOrderHistory.do")
	OkCoinOrderResult getOrderHistory(@FormParam("partner") long partner, @FormParam("symbol") String symbol, @FormParam("status") String status,
			@FormParam("currentPage") String currentPage, @FormParam("pageLength") String pageLength, @FormParam("sign") ParamsDigest sign) throws IOException;

}
