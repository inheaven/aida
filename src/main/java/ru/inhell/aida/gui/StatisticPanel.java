package ru.inhell.aida.gui;

import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.trader.AlphaTraderBean;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.04.11 16:46
 */
public class StatisticPanel extends JPanel {
    private JTable table = new JTable();
    private final static String[] COLUMNS_NAMES = {"Название", "Изменение", "Сделка", "Цена", "Заявки", "Остановки",
            "Баланс", "Оценка", "Баланс (пред.)", "Оценка (пред.)", "Баланс (все)", "Оценка (все)"};

    public StatisticPanel() {
        AlphaTraderBean alphaTraderBean = AidaInjector.getInstance(AlphaTraderBean.class);

        final List<AlphaTrader> alphaTraders = alphaTraderBean.getAlphaTraders();

        table.setModel(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return alphaTraders.size();
            }

            @Override
            public int getColumnCount() {
                return COLUMNS_NAMES.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return null;
            }
        });





    }
}
