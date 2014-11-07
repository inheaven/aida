package com.xeiam.xchange.okcoin;

import java.util.HashMap;
import java.util.Map;

public class OkCoinUtils {
    private static Map<Integer, String> ERROR_CODES = new HashMap<Integer, String>(){{
        put(20001, "user does not exist");
        put(20002, "user frozen");
        put(20003, "frozen due to force liquidation");
        put(20004, "future account frozen");
        put(20005, "user future account does not exist");
        put(20006, "required field can not be null");
        put(20007, "illegal parameter");
        put(20008, "future account fund balance is zero");
        put(20009, "future contract status error");
        put(20010, "risk rate information does not exist");
        put(20011, "risk rate bigger than 90% before opening position");
        put(20012, "risk rate bigger than 90% after opening position");
        put(20013, "temporally no counter party price");
        put(20014, "system error");
        put(20015, "order does not exist");
        put(20016, "liquidation quantity bigger than holding");
        put(20017, "not authorized/illegal order ID");
        put(20018, "order price higher than 105% or lower than 95% of the price of last minute");
        put(20019, "IP restrained to access the resource");
        put(20020, "secret key does not exist");
        put(20021, "index information does not exist");
        put(20022, "wrong API interface");
    }};



    public static String getErrorMessage(int errorCode) {

    switch (errorCode) {

    case (10000):
      return "Required field can not be null";
    case (10001):
      return "User request too frequent";
    case (10002):
      return "System error";
    case (10003):
      return "Try again later";
    case (10004):
      return "Not allowed to get resource due to IP constraint";
    case (10005):
      return "secretKey does not exist";
    case (10006):
      return "Partner (API key) does not exist";
    case (10007):
      return "Signature does not match";
    case (10008):
      return "Illegal parameter";
    case (10009):
      return "Order does not exist";
    case (10010):
      return "Insufficient funds";
    case (10011):
      return "Order quantity is less than minimum quantity allowed";
    case (10012):
      return "Invalid currency pair";
    case (10013):
      return "Only support https request";
    case (10014):
      return "Order price can not be ≤ 0 or ≥ 1,000,000";
    case (10015):
      return "Order price differs from current market price too much";
    case (10216):
      return "Non-public API";
    default:
        String code =  ERROR_CODES.get(errorCode);

      return code != null ?  code : "Error Code " + errorCode;
    }
  }
}
