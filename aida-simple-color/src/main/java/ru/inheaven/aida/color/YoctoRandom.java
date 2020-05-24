package ru.inheaven.aida.color;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YColorLed;
import okhttp3.*;
import org.json.JSONArray;
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

    private static boolean start = false;

    public static void main(String[] args) throws IOException {
        startColor();
    }

    private static void startColor() {
        try {
            YAPI.RegisterHub("127.0.0.1");

            YColorLed led1 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed1");
            YColorLed led2 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed2");
                        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                LocalDateTime localDateTime = LocalDateTime.now();

                if (((localDateTime.getMinute() + 1) % 10 == 0 && localDateTime.getSecond() == 59) || !start){
                    int c = localDateTime.getHour() > 6 && localDateTime.getHour() < 23  ? 255 : 32;

                    try {
                        int[] ints = nextInts(c);

                        if (ints == null){
                            return;
                        }

                        int r = ints[0];
                        int g = ints[1];
                        int b = ints[2];

                        int rgbColor = ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);

                        led1.setRgbColor(rgbColor);
                        led2.setRgbColor(rgbColor);

                        start = true;
                    } catch (YAPI_Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println(LocalDateTime.now().toString() + " " + e.getLocalizedMessage());
        }
    }

    private static int[] nextInts(int bound){
        try {
            JSONObject json = new JSONObject();

            json.put("jsonrpc", "2.0");
            json.put("method","generateIntegers");

            JSONObject params = new JSONObject();

            params.put("apiKey", "8e9097d6-6a82-4234-a48e-d94577046b78");
            params.put("n", 3);
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

                JSONArray data = res.getJSONObject("result").getJSONObject("random").getJSONArray("data");

                int[] ints = new int[3];

                ints[0] = data.getInt(0);
                ints[1] = data.getInt(1);
                ints[2] = data.getInt(2);

                return putInts(ints);
            }

        } catch (Exception e) {
            System.err.println(LocalDateTime.now().toString() + " " + e.getLocalizedMessage());
        }

        return null;
    }

    private static int[] putInts(int[] ints) throws IOException {
        JSONObject json = new JSONObject();

        json.put("data0", ints[0]);
        json.put("data1", ints[1]);
        json.put("data2", ints[2]);
        json.put("timestamp", LocalDateTime.now(ZoneId.of("UTC")).toString());

        Request request = new Request.Builder()
                .url("http://aida:9200/aida/_doc")
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        try(Response response = ELASTIC.newCall(request).execute()) {
            response.code();
        }

        return ints;
    }
}
