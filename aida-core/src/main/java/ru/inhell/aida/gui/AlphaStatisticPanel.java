package ru.inhell.aida.gui;

import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.entity.AlphaTraderData;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.statistic.AlphaStatisticService;
import ru.inhell.aida.trader.AlphaTraderBean;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.04.11 16:46
 */
public class AlphaStatisticPanel extends JPanel {
    private final static DateFormat TIME_FORMAT = DateFormat.getTimeInstance();

    private JTable table;
    private final static String[] COLUMNS_NAMES = {
            "Название",
            "Изменение",
            "Сделка",
            "Количество",
            "Цена",
            "Время",
            "Заявки",
            "Остановки",
            "Баланс",
            "Оценка",
            "Баланс (пред.)",
            "Оценка (пред.)",
            "Баланс (все)",
            "Оценка (все)"};

    private AlphaTraderBean alphaTraderBean = AidaInjector.getInstance(AlphaTraderBean.class);
    private AlphaStatisticService alphaStatisticService = AidaInjector.getInstance(AlphaStatisticService.class);

    public AlphaStatisticPanel() {
        final List<AlphaTrader> alphaTraders = alphaTraderBean.getAlphaTraders();

        table = new JTable();
        add(table);

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
                AlphaTrader alphaTrader = alphaTraders.get(rowIndex);
                AlphaTraderData currentAlphaTraderData = alphaTraderBean.getCurrentAlphaTraderData(alphaTrader.getId());
                Long id = alphaTrader.getId();

                switch (columnIndex){
                    case 0:
                        return alphaTrader.getName();
                    case 1:
                        return 100 * alphaStatisticService.getCurrentBalance(id) / alphaStatisticService.getPreviousBalance(id);
                    case 2:
                        return currentAlphaTraderData.getOrder().name();
                    case 3:
                        return currentAlphaTraderData.getQuantity();
                    case 4:
                        return currentAlphaTraderData.getPrice();
                    case 5:
                        return TIME_FORMAT.format(currentAlphaTraderData.getDate());
                    case 6:
                        return alphaStatisticService.getCurrentOrderCount(id);
                    case 7:
                        return alphaTrader.getAlphaOracle().getStopCount();
                    case 8:
                        return alphaStatisticService.getCurrentBalance(id);
                    case 9:
                        return alphaStatisticService.getCurrentScore(id);
                    case 10:
                        return alphaStatisticService.getPreviousBalance(id);
                    case 11:
                        return alphaStatisticService.getPreviousScore(id);
                    case 12:
                        return alphaStatisticService.getAllBalance(id);
                    case 13:
                        return alphaStatisticService.getAllScore(id);
                }

                return null;
            }
        });

        process();
    }

    public void process(){
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                table.updateUI();

            }
        }, 0, 30, TimeUnit.SECONDS);
    }
}
