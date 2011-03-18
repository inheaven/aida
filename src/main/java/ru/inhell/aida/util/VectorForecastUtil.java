package ru.inhell.aida.util;

import ru.inhell.aida.entity.VectorForecastData;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.03.11 15:26
 */
public class VectorForecastUtil {
    public static boolean isMax(List<VectorForecastData> data, int index, int delta){
        if (index + delta >= data.size() || index - delta < 0){
            return false;
        }


        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getPrice() <= data.get(index + i + 1).getPrice()) return false;
            if (data.get(index - i).getPrice() <= data.get(index - i - 1).getPrice()) return false;
        }

        return true;
    }

    public static boolean isMax(float[] forecast, int index, int delta){
        if (index + delta >= forecast.length || index - delta < 0){
            return false;
        }


        for (int i = 0; i < delta; ++i){
            if (forecast[index + i] <= forecast[index + i + 1]) return false;
            if (forecast[index - i] <= forecast[index - i - 1]) return false;
        }

        return true;
    }

    public static boolean isMin(List<VectorForecastData> data, int index, int delta){
        if (index + delta >= data.size() || index - delta < 0){
            return false;
        }

        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getPrice() >= data.get(index + i + 1).getPrice()) return false;
            if (data.get(index - i).getPrice() >= data.get(index - i - 1).getPrice()) return false;
        }

        return true;
    }

    public static boolean isMin(float[] forecast, int index, int delta){
        if (index + delta >= forecast.length || index - delta < 0){
            return false;
        }

        for (int i = 0; i < delta; ++i){
            if (forecast[index + i] >= forecast[index + i + 1]) return false;
            if (forecast[index - i] >= forecast[index - i - 1]) return false;
        }

        return true;
    }
}
