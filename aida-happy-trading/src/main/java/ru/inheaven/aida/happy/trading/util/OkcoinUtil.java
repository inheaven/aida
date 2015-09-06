package ru.inheaven.aida.happy.trading.util;

import com.xeiam.xchange.okcoin.FuturesContract;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.SymbolType;

/**
 * @author inheaven on 01.09.2015 2:24.
 */
public class OkcoinUtil {
    public static String toSymbol(String symbol){
        if (symbol.equals("BTC/USD")){
            return "btc_usd";
        }else if (symbol.equals("LTC/USD")){
            return "ltc_usd";
        }

        throw new IllegalArgumentException("error to symbol -> " + symbol);
    }

    public static String toContractName(SymbolType symbolType){
        switch (symbolType){
            case THIS_WEEK: return "this_week";
            case NEXT_WEEK: return "next_week";
            case QUARTER: return "quarter";
        }

        throw new IllegalArgumentException("error to contract name -> " + symbolType);
    }

    public static FuturesContract toContract(SymbolType symbolType){
        switch (symbolType){
            case THIS_WEEK: return FuturesContract.ThisWeek;
            case NEXT_WEEK: return FuturesContract.NextWeek;
            case QUARTER: return FuturesContract.Quarter;
        }

        throw new IllegalArgumentException("error to contract name -> " + symbolType);
    }

    public static FuturesContract getFuturesContract(Order order) {
        FuturesContract futuresContract;

        //noinspection Duplicates
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
        return futuresContract;
    }

    public static OrderStatus getOrderStatus(int status){
        switch (status) {
            case -1:
            case 4:
                return OrderStatus.CANCELED;
            case 2:
                return OrderStatus.CLOSED;
            default:
                return OrderStatus.OPEN;
        }

    }
}
