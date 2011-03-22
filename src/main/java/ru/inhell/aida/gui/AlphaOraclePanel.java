package ru.inhell.aida.gui;

import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 16:55
 */
public class AlphaOraclePanel extends JPanel{
    public AlphaOraclePanel() {
        setLayout(new GridLayout(0,1));

        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);

        for (AlphaOracle ao : alphaOracleBean.getAlphaOracles()){
            VectorForecast vf = ao.getVectorForecast();

            JPanel panel = new JPanel();

            panel.add(new JLabel(ao.getId() + ". " + vf.getSymbol() + "-" + vf.getInterval().name() + "-"
                    + "n" + vf.getN() + "l" + vf.getL() + "p" + vf.getP() + "m" + vf.getM()));

            panel.add(new JLabel("[prediction]"));

            panel.add(new JButton("Запустить"));

            add(panel);
        }
    }
}
