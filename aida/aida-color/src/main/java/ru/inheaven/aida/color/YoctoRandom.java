package ru.inheaven.aida.color;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YColorLed;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly Ivanov
 * 23.04.2020 1:58 PM
 */
public class YoctoRandom {
    public static void main(String[] args) {
        try {
            YAPI.RegisterHub("127.0.0.1");

            Random random = new SecureRandom();

            YColorLed led1 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed1");
            YColorLed led2 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed2");

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                int r = random.nextInt(255);
                int g = random.nextInt(255);
                int b = random.nextInt(255);

                try {
                    led1.setRgbColor(((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff));
                    led2.setRgbColor(((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff));
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
            }, 0, 15, TimeUnit.MINUTES);
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
    }
}
