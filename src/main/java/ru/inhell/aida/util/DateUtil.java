package ru.inhell.aida.util;

import ru.inhell.aida.entity.VectorForecast;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:11
 */
public class DateUtil {
    public static Date now(){
        return new Date();
    }

    public static Date nowMsk(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -3);

        return calendar.getTime();
    }

    public static Date nextMinute(Date date){
        return getOneMinuteIndexDate(date, 1);
    }

    public static Date getOneMinuteIndexDate(Date date, int index){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, index);

        return calendar.getTime();
    }

    public static int getMinuteShift(Date from, Date to){
        if (to == null || from == null){
            return -1;
        }

        return (int) ((from.getTime() - to.getTime())/1000/60);
    }

    public static long getAbsMinuteShiftMsk(Date date){
        return Math.abs(nowMsk().getTime() - date.getTime())/1000/60;
    }

    public static boolean isSameDay(Date date1, Date date2){
        if (date1 == null || date2 == null){
            return false;
        }

        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameMinute(Date date1, Date date2){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
                && c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)
                && c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);

    }

    public static Date getCurrentStartTradeTime(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    public static Date getCurrentEndTradeTime(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 45);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    public static Date getPreviousStartTradeTime(){
        Calendar c = Calendar.getInstance();

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
            c.add(Calendar.DAY_OF_YEAR, -3);
        }else{
            c.add(Calendar.DAY_OF_YEAR, -1);
        }

        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    public static Date getPreviousEndTradeTime(){
        Calendar c = Calendar.getInstance();

        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
            c.add(Calendar.DAY_OF_YEAR, -3);
        }else{
            c.add(Calendar.DAY_OF_YEAR, -1);
        }

        c.set(Calendar.HOUR_OF_DAY, 18);
        c.set(Calendar.MINUTE, 45);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }
}
