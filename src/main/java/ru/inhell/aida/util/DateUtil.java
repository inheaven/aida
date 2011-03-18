package ru.inhell.aida.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:11
 */
public class DateUtil {
    public static Date now(){
        return new Date();
    }

    public static Date nextMinute(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, 1);

        return calendar.getTime();
    }
}
