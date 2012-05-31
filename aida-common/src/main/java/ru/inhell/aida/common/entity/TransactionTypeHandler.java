package ru.inhell.aida.common.entity;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 17:45
 */
public class TransactionTypeHandler extends BaseTypeHandler<TransactionType> {
    public static final String BUY_STRING = "Купля";
    public static final String SELL_STRING = "Продажа";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TransactionType parameter, JdbcType jdbcType) throws SQLException {
        switch (parameter) {
            case BUY:
                ps.setString(i, BUY_STRING);
            case SELL:
                ps.setString(i, SELL_STRING);
        }
    }

    @Override
    public TransactionType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getTransactionType(rs.getString(columnName));
    }

    @Override
    public TransactionType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getTransactionType(rs.getString(columnIndex));
    }

    @Override
    public TransactionType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getTransactionType(cs.getString(columnIndex));
    }

    private TransactionType getTransactionType(String s){
        if (s != null){
            switch (s){
                case BUY_STRING:
                    return TransactionType.BUY;
                case SELL_STRING:
                    return TransactionType.SELL;
            }
        }

        return null;
    }
}
