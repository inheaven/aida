package ru.inhell.aida.oracle;

import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.Prediction;
import ru.inhell.aida.entity.Quote;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 18:57
 */
public interface IAlphaOracleListener {
    void predicted(AlphaOracle alphaOracle, Prediction prediction, List<Quote> quotes, float[] forecast);
    Long getFilteredId();
}
