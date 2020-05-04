package ru.inheaven.aida.color;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YColorLed;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly Ivanov
 * 23.04.2020 1:58 PM
 */
public class YoctoRandom {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String URL = "https://api.random.org/json-rpc/2/invoke";

    private static final OkHttpClient RANDOM = new OkHttpClient();

    private static final OkHttpClient ELASTIC = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        try {
            YAPI.RegisterHub("127.0.0.1");

            YColorLed led1 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed1");
            YColorLed led2 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed2");

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                LocalDateTime localDateTime = LocalDateTime.now();

                if ((localDateTime.getMinute() + 1) % 10 == 0 && localDateTime.getSecond() == 59){
                    int c = localDateTime.getHour() > 8 && localDateTime.getHour() < 23 ? 255 : 32;

                    try {
                        int r = nextInt(c);
                        int g = nextInt(c);
                        int b = nextInt(c);

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
            JSONObject json = new JSONObject();

            json.put("jsonrpc", "2.0");
            json.put("method","generateIntegers");

            JSONObject params = new JSONObject();

            params.put("apiKey", "8e9097d6-6a82-4234-a48e-d94577046b78");
            params.put("n", 1);
            params.put("min", 1);
            params.put("max", bound);

            json.put("params", params);

            Long id = System.nanoTime();

            json.put("id", id);

            Request request = new Request.Builder()
                    .url(URL)
                    .post(RequestBody.create(json.toString(), JSON))
                    .build();

            try (Response response = RANDOM.newCall(request).execute()) {
                JSONObject res = new JSONObject(Objects.requireNonNull(response.body()).string());

                if (res.getLong("id") != id){
                    throw new RuntimeException(id + " " + res);
                }

                int num = res.getJSONObject("result").getJSONObject("random").getJSONArray("data").getInt(0);

                putInt(num);

                return num;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static void putInt(int num) throws IOException {
        JSONObject json = new JSONObject();

        json.put("data", num);
        json.put("timestamp", LocalDateTime.now(ZoneId.of("UTC")).toString());

        Request request = new Request.Builder()
                .url("http://aida:9200/aida-simple-color/_doc")
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        ELASTIC.newCall(request).execute();
    }
}
