package ru.inheaven.aida.svet;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.common.util.concurrent.AtomicDouble;
import org.magcode.sunricher.mqtt.TcpClient;
import org.sunricher.wifi.api.ColorHandlerImpl;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Anatoly A. Ivanov
 * 07.04.2018 9:51
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("sun.java2d.opengl", "true");

        TcpClient tcpClient = new TcpClient("192.168.0.104", 8899);
        tcpClient.init();

        java.util.List<Integer> channel = Collections.singletonList(1);

        ColorHandlerImpl colorHandler = new ColorHandlerImpl(tcpClient);

        colorHandler.togglePower(channel,true);

        colorHandler.setW(channel, 0);
        colorHandler.setRGBBrightness(channel, (byte) 8);

        Webcam webcam = Webcam.getDefault();
        webcam.setCustomViewSizes(new Dimension[]{new Dimension(1280, 720)});

        int w = 1280;
        int h = 720;

        webcam.setViewSize(new Dimension(w, h));
        webcam.open();

        setUIFont(new javax.swing.plaf.FontUIResource("Serif",Font.PLAIN,24));

        JFrame window = new JFrame("Svet");
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        window.add(container);

        WebcamPanel webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);

        container.add(webcamPanel);

        JPanel brightness = new JPanel();
        brightness.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        container.add(brightness);

        JTextField rgbBrightness, redBrightness, greenBrightness, blueBrightness, white;
        JButton apply;

        brightness.add(redBrightness = new JTextField("1", 10));
        brightness.add(greenBrightness = new JTextField("1", 10));
        brightness.add(blueBrightness = new JTextField("1", 10));
        brightness.add(rgbBrightness = new JTextField("0.1", 10));
        brightness.add(white = new JTextField("0", 10));
        brightness.add(apply = new JButton("Apply"));

        AtomicDouble rBr = new AtomicDouble(1);
        AtomicDouble gBr = new AtomicDouble(1);
        AtomicDouble bBr = new AtomicDouble(1);

        apply.addActionListener(a -> {
            try {
                rBr.set(Double.parseDouble(redBrightness.getText()));
                gBr.set(Double.parseDouble(greenBrightness.getText()));
                bBr.set(Double.parseDouble(blueBrightness.getText()));

                colorHandler.setW(channel, (byte) (Double.parseDouble(white.getText())*255));
                colorHandler.setRGBBrightness(channel, (byte) (Double.parseDouble(rgbBrightness.getText())*255));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

        AtomicReference<Color> color = new AtomicReference<>(Color.WHITE);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                BufferedImage image = webcam.getImage();

                Color meanColor = meanColor(image);

                meanColor = new Color((int) (meanColor.getRed()*rBr.get()),
                        (int) (meanColor.getGreen()*gBr.get()),
                        (int) (meanColor.getBlue()*bBr.get()));

                if (!color.get().equals(meanColor)) {
                    Byte r = null;
                    Byte g = null;
                    Byte b = null;

                    if (color.get().getRed() != meanColor.getRed()){
                        r = (byte) (Math.max(0, meanColor.getRed()));
                    }

                    if (color.get().getGreen() != meanColor.getGreen()){
                        g = (byte) (Math.max(0, meanColor.getGreen()));
                    }

                    if (color.get().getBlue() != meanColor.getBlue()){
                        b = (byte) (Math.max(0, meanColor.getBlue()));
                    }

                    System.out.println();
                    System.out.println(meanColor.getRed() + " " + meanColor.getGreen() + " " + meanColor.getBlue());

                    colorHandler.setRGB(channel, r, g, b);
                }

                color.set(meanColor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
            try {
                if (LocalTime.now().getHour() > 5) {
                    ImageIO.write(webcam.getImage(), "JPG", new File("f:/svet/" +
                            LocalDateTime.now().toString().replace(":", "_").replace(".", "_") +
                    ".jpg"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.MINUTES);


    }

    public static Color meanColor(BufferedImage image){
        long redBucket = 0;
        long greenBucket = 0;
        long blueBucket = 0;
        long pixelCount = 0;

        for (int y = 0; y < image.getHeight(); y++){
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));

                pixelCount++;
                redBucket += c.getRed();
                greenBucket += c.getGreen();
                blueBucket += c.getBlue();
            }
        }

        float brightness = 0.1f;

        return new Color((int) (brightness * redBucket / pixelCount),
                (int) (brightness * greenBucket / pixelCount),
                (int) (brightness * blueBucket / pixelCount));
    }

    public static void setUIFont (javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }
}
