package ru.inheaven.aida.coin.cexio;

import com.xeiam.xchange.cexio.dto.marketdata.CexIODepth;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

/**
 * @author Anatoly Ivanov
 *         Date: 012 12.08.14 19:07
 */
public interface CexIO extends com.xeiam.xchange.cexio.CexIO {
    @GET
    @Path("order_book/{ident}/{currency}")
    CexIODepth getDepth(@PathParam("ident") String tradeableIdentifier, @PathParam("currency") String currency,
                        @FormParam("since") int depth) throws IOException;
}
