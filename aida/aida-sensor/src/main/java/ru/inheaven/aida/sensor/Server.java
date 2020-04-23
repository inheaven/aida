package ru.inheaven.aida.sensor;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 * 27.05.2018 0:50
 */
public class Server {
    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {
        while (true) {
            int index = 0;

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("D:\\Java\\AIDA\\aida-sensor\\keys\\server-test.jks"),
                    "fbhffbhf".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore,"fbhffbhf".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

            SSLServerSocketFactory sslSocketFactory = sslContext.getServerSocketFactory();


            try (SSLServerSocket sslServerSocket = (SSLServerSocket) sslSocketFactory.createServerSocket(12345)) {
                sslServerSocket.setSoTimeout(30 * 60 * 1000);

                try (SSLSocket socket = (SSLSocket) sslServerSocket.accept()) {
                    System.out.println(socket.getRemoteSocketAddress() + " " + socket.getSession().getProtocol() + " " +
                            socket.getSession().getCipherSuite());

                    socket.setSoTimeout(30 * 60 * 1000);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("Cp1251")));
                    String input;

                    long time = System.currentTimeMillis();

                    List<String> list = new ArrayList<>();

                    try {
                        while ((input = in.readLine()) != null) {
                            list.add(input);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    long time2 = System.currentTimeMillis();

                    InfluxDB influxDB = Influx.getInstance().getInfluxDB();

                    for (String s : list) {
                        try {
                            String[] data = s.split(":");

                            Point.Builder pointBuilder = Point.measurement("sensor" + data[0])
                                    .time(Long.parseLong(data[2]), TimeUnit.MILLISECONDS)
                                    .tag("type", data[1])
                                    .addField("timestamp", Long.parseLong(data[3]))
                                    .addField("accuracy", Long.parseLong(data[4]));

                            String[] sValues = data[5].replace("[", "").replace("]", "").split(",");

                            for (int i = 0; i < sValues.length; ++i) {
                                pointBuilder.addField("value" + i, Float.parseFloat(sValues[i].trim()));
                            }

                            influxDB.write(pointBuilder.build());

                            index++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(new Date() + " " + index + " " + (time2 - time) + " " + (System.currentTimeMillis() - time2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
