package com.xeiam.xchange.okcoin.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinErrorResult;

import java.util.Map;

/**
 * @author Anatoly Ivanov
 *         Date: 06.11.2014 22:38
 */
public class OkCoinFutureUserInfo extends OkCoinErrorResult {
    private Map<String, OkCoinFutureInfo> futureInfoMap;


    public OkCoinFutureUserInfo(@JsonProperty("result") boolean result, @JsonProperty("error_code") int errorCode,
                                @JsonProperty("info") Map<String, OkCoinFutureInfo> futureInfoMap) {
        super(result, errorCode);

        this.futureInfoMap = futureInfoMap;
    }

    public Map<String, OkCoinFutureInfo> getFutureInfoMap() {
        return futureInfoMap;
    }
}
