package ru.inheaven.aida.okex.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author Anatoly A. Ivanov
 * 24.11.2017 17:16
 */
public interface StrategyMapper {
    @Update("update okex_strategy set created_orders = #{json} where id = #{id}")
    void updateCreatedOrders(@Param("id") Long id, @Param("json") String json);

    @Select("select created_orders from okex_strategy where id = #{id}")
    String getCreatedOrders(Long id);
}
