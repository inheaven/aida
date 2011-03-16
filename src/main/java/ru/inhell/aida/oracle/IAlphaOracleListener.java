package ru.inhell.aida.oracle;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 18:57
 */
public interface IAlphaOracleListener {
    void predicted(String symbol, AlphaOracleType type, Date date, float price);
}
