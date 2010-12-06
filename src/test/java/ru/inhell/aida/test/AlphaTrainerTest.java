package ru.inhell.aida.test;

import org.testng.annotations.Test;
import ru.inhell.aida.oracle.AlphaOracle;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 21:15
 */
public class AlphaTrainerTest {

    @Test
    public void train() throws IOException, ParseException {
        AlphaOracle alphaOracle = new AlphaOracle();
        for (int l=100; l <= 250; l+=25){
            for (int p = 12; p <= 25; p++){
                alphaOracle.train(1000, l, p, 5);
            }
        }
    }

}
