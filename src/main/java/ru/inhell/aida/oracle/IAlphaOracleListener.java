package ru.inhell.aida.oracle;

import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 18:57
 */
public interface IAlphaOracleListener {
    void predicted(AlphaOracle alphaOracle, String symbol, AlphaOracleData.PREDICTION prediction, Date date, float price);
}
