package ru.inheaven.aida.okex.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import ru.inheaven.aida.okex.model.Order;

import java.util.List;

public interface OrderMapper {
    @Insert({"insert into okex_order (strategy_id, avg_price, cl_order_id, commission, total_qty, currency, exec_id, order_id, qty, " +
            "status, type, price, side, symbol, text, tx_time, exec_type, leaves_qty, margin_ratio, created, closed) " +
            "value (#{strategyId}, #{avgPrice}, #{clOrderId}, #{commission}, #{totalQty}, #{currency}, #{execId}, #{orderId}, #{qty}, " +
            "#{status}, #{type}, #{price}, #{side}, #{symbol}, #{text}, #{txTime}, #{execType}, #{leavesQty}, #{marginRatio}, " +
            "#{created}, #{closed})"})
    void insert(Order order);

    @Update("update okex_order set closed = now() where (order_id = #{orderId} or cl_order_id = #{clOrderId}) " +
            "and symbol = #{symbol} and currency = #{currency}")
    void close(Order order);

    @Select("select * " +
            "from okex_order n " +
            "where n.status = 'new' and n.created > date_sub(now(), interval 1 day) and n.cl_order_id is not null " +
            "and (select 1 from okex_order f where n.order_id = f.order_id and f.status = 'filled' limit 1) is null")
    List<Order> getNewOrders();

    @Select("select * from okex_order where symbol = #{symbol} and currency = #{currency} and status = 'filled' and " +
            "type in ('market', 'limit') order by id desc limit #{size}")
    List<Order> getFilledOrders(@Param("symbol") String symbol, @Param("currency") String currency, @Param("size") int size);

}
