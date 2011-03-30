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
        return Calendar.getInstance(TimeZone.getTimeZone("GMT+3:00")).getTime();
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

    public static long getMinuteShift(Date from, Date to){
        return (from.getTime() - to.getTime())/1000/60;
    }


}
