package ru.inheaven.aida.color;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YColorLed;

import java.util.Calendar;

/**
 * @author Anatoly A. Ivanov
 * 01.01.2018 22:43
 */
public class Test {
    public static void main(String[] args) throws YAPI_Exception {
        YAPI.RegisterHub("127.0.0.1");

        double val = 0.662513482853707;

        int r1 = 128;
        int g1 = 0;
        int b1 = 0;

        int r2 = 0;
        int g2 = 128;
        int b2 = 0;

        YColorLed led1 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed1");
        YColorLed led2 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed2");

        int r = 255;//(int) (r1 + val * (r2 - r1));
        int g = 0;//(int) (g1 + val * (g2 - g1));
        int b = 255;//(int) (b1 + val * (b2 - b1));

        led2.setRgbColor(((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff));
        led1.setRgbColor(((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff));

        Calendar calendar = Calendar.getInstance();


        System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
    }
}
