package ru.inhell.aida.gui;

import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.quik.QuikException;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.trader.AlphaTraderService;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 18:37
 */
public class Aida {
    private static JPanel root;

    public static void main(String... args) throws QuikException {
        //Frame
        JFrame frame = new JFrame("Aida");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new GridLayout(3,3));
        frame.add(root);

        process(1, 2, 14, 19, 27, 28, 31, 32, 33);

        frame.pack();
        frame.setVisible(true);

        AidaInjector.getInstance(QuikService.class).connect(ru.inhell.aida.Aida.getQuikDir());
    }

    private static void process(int... ids){
        for (int id :  ids){
            root.add(new AlphaOracleChart((long)id));
            AidaInjector.getInstance(AlphaTraderService.class).process((long)id);
        }
    }
}

