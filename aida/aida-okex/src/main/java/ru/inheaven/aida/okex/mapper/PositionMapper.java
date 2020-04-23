package ru.inheaven.aida.okex.mapper;

import org.apache.ibatis.annotations.Insert;
import ru.inheaven.aida.okex.model.Position;

public interface PositionMapper {
    @Insert({"insert into okex_position (account_id, currency, avg_price, qty, price, symbol, profit, frozen, margin_cash, " +
            "position_id, type, evening_up) " +
            "value (#{accountId}, #{currency}, #{avgPrice}, #{qty}, #{price}, #{symbol}, #{profit}, #{frozen}, #{marginCash}, " +
            "#{positionId}, #{type}, #{eveningUp})"})
    void insert(Position position);
}
