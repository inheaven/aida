package ru.inheaven.aida.okex.mapper;

import org.apache.ibatis.annotations.Insert;
import ru.inheaven.aida.okex.model.Info;

public interface InfoMapper {
    @Insert({"insert into okex_info (account_id, currency, balance, profit, margin_cash) " +
            "value (#{accountId}, #{currency}, #{balance}, #{profit}, #{margin})"})
    void insert(Info info);
}
