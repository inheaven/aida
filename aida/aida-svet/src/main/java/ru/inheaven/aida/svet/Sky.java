package ru.inheaven.aida.svet;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov
 * 01.05.2018 21:07
 */
public class Sky {
    public static void main(String[] args) {
        Webcam webcam = Webcam.getDefault();
        webcam.setCustomViewSizes(new Dimension[]{new Dimension(1280, 720)});

        int w = 1280;
        int h = 720;

        webcam.setViewSize(new Dimension(w, h));

        setUIFont(new javax.swing.plaf.FontUIResource("Serif",Font.PLAIN,24));

        JFrame window = new JFrame("Sky");
        JPanel container = new JPanel(new BorderLayout());
        window.add(container);

        WebcamPanel panel = new WebcamPanel(webcam);
        container.add(panel, BorderLayout.PAGE_START);

//        JPanel color = new JPanel();
//        container.add(color, BorderLayout.PAGE_END);
//
//        JTextField rgbBrightness, redBrightness, greenBrightness, blueBrightness;
//        JButton apply;
//
//        color.add(redBrightness = new JTextField("1", 10));
//        color.add(greenBrightness = new JTextField("1", 10));
//        color.add(blueBrightness = new JTextField("1", 10));
//        color.add(rgbBrightness = new JTextField("0.1", 10));
//        color.add(apply = new JButton("Apply"));
//
//        apply.addActionListener(a -> {
//            System.out.println(redBrightness.getText());
//        });

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
//            try {
//                if (LocalTime.now().getHour() > 4 && LocalTime.now().getHour() < 22) {
//                    if (!webcam.isOpen()){
//                        panel.start();
//                        System.out.println("OPEN");
//                    }
//
//                    ImageIO.write(webcam.getImage(), "PNG", new File("f:/svet/" +
//                            LocalDateTime.now().toString().replace(":", "_").replace(".", "_") +
//                            ".png"));
//                }else{
//                    if (webcam.isOpen()){
//                        panel.stop();
//                        System.out.println("CLOSE");
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }, 0, 1, TimeUnit.MINUTES);


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
