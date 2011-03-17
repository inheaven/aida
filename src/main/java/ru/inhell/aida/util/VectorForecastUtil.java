package ru.inhell.aida.util;

import ru.inhell.aida.entity.VectorForecastData;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.03.11 15:26
 */
public class VectorForecastUtil {
     public boolean isMax(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getClose() <= data.get(index + i + 1).getClose()) return false;
            if (data.get(index - i).getClose() <= data.get(index - i - 1).getClose()) return false;
        }

        return true;
    }

    public boolean isMin(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getClose() >= data.get(index + i + 1).getClose()) return false;
            if (data.get(index - i).getClose() >= data.get(index - i - 1).getClose()) return false;
        }

        return true;
    }
}
