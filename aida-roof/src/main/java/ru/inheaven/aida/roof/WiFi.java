package ru.inheaven.aida.roof;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 * 31.12.2017 22:58
 */
public class WiFi {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String URL = "https://api.random.org/json-rpc/2/invoke";

    private static final OkHttpClient RANDOM = new OkHttpClient();

    private static final OkHttpClient ELASTIC = new OkHttpClient();

    private static boolean start = false;


    public static void main(String args[]) {
        startColor();
    }

    private static byte[] getMessage(byte category, byte channel, byte value) {
        byte[] result = new byte[12];

        result[0] = (byte) 85;
        result[1] = (byte) 43;
        result[2] = (byte) 20;
        result[3] = (byte) -64;
        result[4] = (byte) 2;
        result[5] = (byte) 1;
        result[6] = category;
        result[7] = channel;
        result[8] = value;
        result[9] = (byte) (result[8] + result[7] + result[6] + result[5] + result[4]);
        result[10] = (byte) -86;
        result[11] = (byte) -86;

        return result;
    }

    public static byte[] getPower(boolean powerState){
        return getMessage((byte) 2, (byte) 18, powerState ? (byte) -85 : (byte) -87);
    }

    public static byte[] getR(byte value){
        return getMessage((byte) 8, (byte) 24, value);
    }

    public static byte[] getG(byte value){
        return getMessage((byte) 8, (byte) 25, value);
    }


    public static byte[] getB(byte value) {
        return getMessage((byte) 8, (byte) 32, value);
    }

    public static byte[] setRGBBrightness(byte value) {
        return getMessage((byte) 8, (byte) 35, value);
    }


    public static byte[] getRgbHue(float hue){
        int value = ((97 - Math.round(96.0f * hue)) + 43) % 96;
        if (value == 0) {
            value = 96;
        }

        return getMessage((byte) 1, (byte) 1, (byte) value);
    }

    public static byte[] getW(int value) {
        return getMessage((byte) 8, (byte)33, (byte) value);
    }

    private static void startColor() {
        try {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                LocalDateTime localDateTime = LocalDateTime.now();

                if (((localDateTime.getMinute() + 1) % 7 == 0 && localDateTime.getSecond() == 59) || !start){
                    int c = localDateTime.getHour() > 6 && localDateTime.getHour() < 23  ? 255 : 32;

                    try (Socket s = new Socket("192.168.0.104", 8899)){
                        int[] ints = nextInts(c);

                        if (ints == null){
                            return;
                        }

                        s.getOutputStream().write(getR((byte) ints[0]));
                        s.getOutputStream().flush();
                        s.getOutputStream().write(getG((byte) ints[1]));
                        s.getOutputStream().flush();
                        s.getOutputStream().write(getB((byte) ints[2]));
                        s.getOutputStream().flush();

                        start = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println(LocalDateTime.now().toString() + " " + e.getLocalizedMessage());
        }
    }

    private static float getHue(int red, int green, int blue) {

        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);

        if (min == max) {
            return 0;
        }

        float hue = 0f;
        if (max == red) {
            hue = (green - blue) / (max - min);

        } else if (max == green) {
            hue = 2f + (blue - red) / (max - min);

        } else {
            hue = 4f + (red - green) / (max - min);
        }

        hue = hue * 60;
        if (hue < 0) hue = hue + 360;

        return hue;
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
