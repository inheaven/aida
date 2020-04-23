package ru.inheaven.aida.okex.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ru.inheaven.aida.okex.model.Trade;

import java.util.List;

public interface TradeMapper {
    @Select("select * from okex_trade where symbol = #{symbol} and currency = #{currency} order by id desc limit #{size}")
    List<Trade> getLastTrades(@Param("symbol") String symbol, @Param("currency") String currency, @Param("size") int size);

    @Select("select * from okex_trade where symbol = #{symbol} and currency = #{currency} and created > date_sub(now(), interval ${interval}) order by created asc")
    List<Trade> getLastIntervalTrades(@Param("symbol") String symbol, @Param("currency") String currency, @Param("interval") String interval);

    @Insert("insert into okex_trade (currency, orig_time, symbol, order_id, side, price, qty) " +
            "value (#{currency}, #{origTime}, #{symbol}, #{orderId}, #{side}, #{price}, #{qty})")
    void insert(Trade trade);
}
