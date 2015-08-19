package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Strategy;

import java.util.List;

/**
 * @author inheaven on 29.06.2015 21:09.
 */
public class StrategyMapper extends BaseMapper<Strategy> {
    public List<Strategy> getActiveStrategies(){
        return sqlSession().selectList("selectActiveStrategies");
    }

    public List<Strategy> getStrategies(){
        return sqlSession().selectList("selectStrategies");
    }
}
