package ru.inheaven.aida.svet;

import org.magcode.sunricher.mqtt.TcpClient;
import org.sunricher.wifi.api.ColorHandlerImpl;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Anatoly A. Ivanov
 * 24.11.2018 19:40
 */
public class Ui {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("sun.java2d.opengl", "true");

        TcpClient tcpClient = new TcpClient("192.168.0.104", 8899);
        tcpClient.init();

        java.util.List<Integer> channel = Collections.singletonList(1);

        ColorHandlerImpl colorHandler = new ColorHandlerImpl(tcpClient);

        colorHandler.togglePower(channel,true);

        setUIFont(new javax.swing.plaf.FontUIResource("Serif",Font.PLAIN,24));

        JFrame frame = new JFrame("Svet UI");
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        frame.add(container);

        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        container.add(panel);

        JTextField rgbBrightness, redBrightness, greenBrightness, blueBrightness, white;
        JButton apply;

        panel.add(redBrightness = new JTextField("0", 10));
        panel.add(greenBrightness = new JTextField("0", 10));
        panel.add(blueBrightness = new JTextField("0", 10));
        panel.add(rgbBrightness = new JTextField("0", 10));
        panel.add(white = new JTextField("0", 10));
        panel.add(apply = new JButton("Apply"));

        apply.addActionListener(a -> {
            try {
                colorHandler.setR(channel, (byte) (Double.parseDouble(redBrightness.getText())));
                colorHandler.setG(channel, (byte) (Double.parseDouble(greenBrightness.getText())));
                colorHandler.setB(channel, (byte) (Double.parseDouble(blueBrightness.getText())));
                colorHandler.setW(channel, (byte) (Double.parseDouble(white.getText())));
                colorHandler.setRGBBrightness(channel, (byte) (Double.parseDouble(rgbBrightness.getText())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }
}
