package ru.inheaven.aida.color;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YColorLed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
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

            YColorLed led1 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed1");
            YColorLed led2 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed2");

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                LocalDateTime localDateTime = LocalDateTime.now();

                if ((localDateTime.getMinute() + 1) % 10 == 0 && localDateTime.getSecond() == 59){
                    int c = localDateTime.getHour() > 8 && localDateTime.getHour() < 23 ? 256 : 32;

                    int r = nextInt(c);
                    int g = nextInt(c);
                    int b = nextInt(c);

                    try {
                        int rgbColor = ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);

                        led1.setRgbColor(rgbColor);
                        led2.setRgbColor(rgbColor);
                    } catch (YAPI_Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
    }

    private static int nextInt(int bound){
        try {
            URL url = new URL("https://www.random.org/integers/?num=1&min=0&col=1&base=10&format=plain&rnd=new&max=" + bound);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                return Integer.parseInt(in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
