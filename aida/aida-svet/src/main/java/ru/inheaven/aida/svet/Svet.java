package ru.inheaven.aida.svet;

import org.magcode.sunricher.mqtt.TcpClient;
import org.sunricher.wifi.api.ColorHandlerImpl;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Anatoly A. Ivanov
 * 18.05.2018 22:17
 */
public class Svet {
    public static void main(String[] args) throws IOException, InterruptedException {
        TcpClient tcpClient = new TcpClient("192.168.0.104", 8899);
        tcpClient.init();

        java.util.List<Integer> channel = Collections.singletonList(1);

        ColorHandlerImpl colorHandler = new ColorHandlerImpl(tcpClient);


        //connect = new Buffer([0, 1, 1, 3, 3, 7]);
        //new Buffer([86, red, green, blue, 170]

        colorHandler.send(new byte[]{(byte) 0, (byte) 1, (byte) 1, (byte) 3, (byte) 3, (byte) 7});
        colorHandler.send(new byte[]{(byte) 86,(byte) 0, (byte) 0, (byte) 128, (byte) 170});

        System.out.println("!231213");
    }
}
