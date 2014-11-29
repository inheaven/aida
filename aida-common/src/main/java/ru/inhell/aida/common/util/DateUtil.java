package ru.inhell.aida.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:11
 */
public class DateUtil {
    private final static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<>();
    private final static ThreadLocal<SimpleDateFormat> SIMPLE_DAY_FORMAT_THREAD_LOCAL = new ThreadLocal<>();
    private final static ThreadLocal<SimpleDateFormat> SIMPLE_TIME_FORMAT_THREAD_LOCAL = new ThreadLocal<>();

    public static SimpleDateFormat simpleDateFormat(){
        SimpleDateFormat simpleDateFormat = SIMPLE_DATE_FORMAT_THREAD_LOCAL.get();

        if (simpleDateFormat == null){
            simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
            SIMPLE_DATE_FORMAT_THREAD_LOCAL.set(simpleDateFormat);
        }

        return simpleDateFormat;
    }

    public static SimpleDateFormat simpleDayFormat(){
        SimpleDateFormat simpleDateFormat = SIMPLE_DAY_FORMAT_THREAD_LOCAL.get();

        if (simpleDateFormat == null){
            simpleDateFormat = new SimpleDateFormat("dd.MM.yy");
            SIMPLE_DAY_FORMAT_THREAD_LOCAL.set(simpleDateFormat);
        }

        return simpleDateFormat;
    }

    public static SimpleDateFormat simpleTimeFormat(){
        SimpleDateFormat simpleDateFormat = SIMPLE_TIME_FORMAT_THREAD_LOCAL.get();

        if (simpleDateFormat == null){
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            SIMPLE_TIME_FORMAT_THREAD_LOCAL.set(simpleDateFormat);
        }

        return simpleDateFormat;
    }

    public static String getString(Date date){
        return simpleDateFormat().format(date);
    }

    public static String getDayString(Date date){
        return simpleDayFormat().format(date);
    }

    public static Date now(){
        return new Date();
    }

    public static Date nowMsk(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(HOUR_OF_DAY, -3);

        return calendar.getTime();
    }

    public static Date nextMinute(Date date){
        return getOneMinuteIndexDate(date, 1);
    }

    public static Date getOneMinuteIndexDate(Date date, int index){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(MINUTE, index);

        return calendar.getTime();
    }

    public static Calendar getCalendar(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    public static Date addDay(Date date, int shift){
        Calendar calendar = getCalendar(date);

        calendar.add(DAY_OF_YEAR, -1);

        return calendar.getTime();
    }

    public static Date getFiveSecondIndexDate(Date date, int index){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, index*5);

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
                && c1.get(HOUR_OF_DAY) == c2.get(HOUR_OF_DAY)
                && c1.get(MINUTE) == c2.get(MINUTE);

    }

    public static Date getCurrentStartTradeTime(){
        Calendar c = Calendar.getInstance();
        c.set(HOUR_OF_DAY, 10);
        c.set(MINUTE, 30);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    public static Date getCurrentEndTradeTime(){
        Calendar c = Calendar.getInstance();
        c.set(HOUR_OF_DAY, 18);
        c.set(MINUTE, 45);
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

        c.set(HOUR_OF_DAY, 10);
        c.set(MINUTE, 30);
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

        c.set(HOUR_OF_DAY, 18);
        c.set(MINUTE, 45);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    @SuppressWarnings("MagicConstant")
    public static Date getDate(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month-1, day);

        return calendar.getTime();
    }

    public static String getTimeString(Date date){
        return simpleTimeFormat().format(date);
    }
}
