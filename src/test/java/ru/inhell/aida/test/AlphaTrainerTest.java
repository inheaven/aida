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
    public void train1() throws IOException, ParseException {
        AlphaOracle alphaOracle = new AlphaOracle();
            for (int p = 13; p < 20; p++){
                alphaOracle.train(1024, 512, p, 16);
            }        
    }

    @Test
    public void train2() throws IOException, ParseException {
        AlphaOracle alphaOracle = new AlphaOracle();
            for (int p = 23; p < 30; p++){
                alphaOracle.train(1024, 512, p, 16);
            }        
    }

    //@Test
    public void train3() throws IOException, ParseException {
        AlphaOracle alphaOracle = new AlphaOracle();
            for (int p = 31; p < 40; p++){
                alphaOracle.train(1024, 512, p, 16);
            }        
    }
}
