package ru.inheaven.aida.color;

import java.net.Socket;
import java.util.Random;

/**
 * @author Anatoly A. Ivanov
 * 31.12.2017 22:58
 */
public class WiFi {
    static Random random = new Random();

    static byte r1 = ((byte) random.nextInt(128));
    static byte r2 = ((byte) random.nextInt(128));
    static byte r3 = ((byte) random.nextInt(128));

    static byte[] send_data;

    static {
        byte[] r0 = new byte[12];
        send_data = r0;
        send_data[1] = r1;
        send_data[2] = r2;
        send_data[3] = r3;
    }

    public static void set_data(byte[] data) {
        int i;

        send_data[5] = 0;
        for (i = 0; i < 3; i++) {
            send_data[i + 6] = data[i];
        }
        byte s = (byte) 0;
        for (i = 4; i < 9; i++) {
            s = (byte) (send_data[i] + s);
        }
        send_data[9] = s;
    }


    public static void main(String args[])
    {
        try{
            Socket s = new Socket("192.168.0.80", 8899);

            byte[] send = Constant.DATA_CDW_BRIGHTNESS_SEEKBAR;
            send[2] = (byte) 255;
            set_data(send);

            for (int i=0;i<10;++i) {
                s.getOutputStream().write(send_data);
                s.getOutputStream().flush();
            }

            // читаем ответ
            byte buf[] = new byte[64*1024];
            int r = s.getInputStream().read(buf);
            String data = new String(buf, 0, r);

            // выводим ответ в консоль
            System.out.println(data);
        }
        catch(Exception e)
        {System.out.println("init error: "+e);} // вывод исключений
    }
}
