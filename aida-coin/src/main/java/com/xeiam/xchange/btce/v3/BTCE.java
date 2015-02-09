package com.xeiam.xchange.btce.v3;

import com.xeiam.xchange.btce.v3.dto.marketdata.BTCEDepthWrapper;
import com.xeiam.xchange.btce.v3.dto.marketdata.BTCEExchangeInfo;
import com.xeiam.xchange.btce.v3.dto.marketdata.BTCETickerWrapper;
import com.xeiam.xchange.btce.v3.dto.marketdata.BTCETradesWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author timmolter
 */
@Path("/")
public interface BTCE {

  @GET
  @Path("api/3/info")
  BTCEExchangeInfo getInfo() throws IOException;

  @GET
  @Path("api/3/ticker/{pairs}")
  @Produces(MediaType.APPLICATION_JSON)
  BTCETickerWrapper getTicker(@PathParam("pairs") String pairs, @DefaultValue("1") @QueryParam("ignore_invalid") int ignoreInvalid)
      throws IOException;

  @GET
  @Path("api/3/depth/{pairs}")
  @Produces(MediaType.APPLICATION_JSON)
  BTCEDepthWrapper getDepth(@PathParam("pairs") String pairs, @DefaultValue("150") @QueryParam("limit") int limit,
      @DefaultValue("1") @QueryParam("ignore_invalid") int ignoreInvalid) throws IOException;

  @GET
  @Path("api/3/trades/{pairs}")
  @Produces(MediaType.APPLICATION_JSON)
  BTCETradesWrapper getTrades(@PathParam("pairs") String pairs, @DefaultValue("1") @QueryParam("limit") int limit,
      @DefaultValue("1") @QueryParam("ignore_invalid") int ignoreInvalid) throws IOException;

}
