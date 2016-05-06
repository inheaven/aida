import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.util.OrderMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author inheaven on 07.10.2015 11:22.
 */
public class PerformanceTest {
    public static void main(String... args){
        OrderMap orderMap = new OrderMap(2);
        ConcurrentHashMap<String, Order> map = new ConcurrentHashMap<>();

        for (int i = 0; i < 1000; ++i){
            Order order = new Order();
            order.setOrderId(String.valueOf(System.nanoTime()));
            order.setInternalId(order.getOrderId());
            order.setPositionId(System.nanoTime());
            order.setStatus(OrderStatus.CREATED);
            order.setType(i % 2 == 0 ? OrderType.BID : OrderType.ASK);

            order.setPrice(new BigDecimal("1540").add(new BigDecimal("0.01").multiply(BigDecimal.valueOf(i))));

            orderMap.put(order);
            map.put(order.getOrderId(), order);
        }



//        BigDecimal price = new BigDecimal("1541");
//        BigDecimal spread = new BigDecimal("0.1");
//
//        long time = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; ++i){
//            orderMap.contains(price, spread, OrderType.ASK);
//        }
//
//        System.out.println(System.currentTimeMillis() - time);
//
//        time = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; ++i){
//            orderMap.get(price, OrderType.ASK);
//            orderMap.get(price, OrderType.BID);
//        }
//
//        System.out.println(System.currentTimeMillis() - time);

//        System.out.println(orderMap.contains(new BigDecimal("1541"), new BigDecimal("0.01"), OrderType.ASK, BigDecimal.ZERO));

//        time = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; ++i){
//            map.searchValues(64, o -> {
//                if (((SELL_SET.contains(o.getType()) && compare(o.getPrice().subtract(price).abs(), spread) <= 0) ||
//                        (BUY_SET.contains(o.getType()) && compare(o.getPrice().subtract(price).abs(), spread) <= 0))){
//                    return o;
//                }
//
//                return null;
//            });
//        }
//        System.out.println(System.currentTimeMillis() - time);
    }

    public static int compare(BigDecimal v1, BigDecimal v2){
        return v1.setScale(2, RoundingMode.HALF_EVEN).compareTo(v2.setScale(2, RoundingMode.HALF_EVEN));
    }
}
