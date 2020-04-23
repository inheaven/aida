package org.sunricher.wifi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcode.sunricher.mqtt.TcpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ColorHandlerImpl implements ColorHandler {
    private static Logger logger = LogManager.getLogger(ColorHandlerImpl.class);
    /**
     * sleep between two commands in a series
     */
    final int SLEEP = 8;

    /**
     * sleep at the end of a command series
     */
    final int SLEEP_AT_END = 100;

    private TcpClient tcpClient;

    public ColorHandlerImpl(TcpClient aTcpClient) {
        tcpClient = aTcpClient;
    }

    @Override
    public void setRGB(List<Integer> zones, Byte r, Byte g, Byte b) throws IOException, InterruptedException {
        send(r != null ? getMessage(zones, (byte) 8, (byte) 24, r) : null,
                g != null ? getMessage(zones, (byte) 8, (byte) 25, g) : null,
                b != null ? getMessage(zones, (byte) 8, (byte) 32, b) : null);
    }

    @Override
    public void setHSV(List<Integer> zones, int h, int s, int v) throws IOException, InterruptedException {

    }

    @Override
    public void setRGBWithWhiteChannel(List<Integer> zones, int r, int g, int b, boolean maxBrightness)
            throws IOException, InterruptedException {

    }

    @Override
    public void setHSVwithWihiteChannel(List<Integer> zones, int h, int s, int v, boolean maxBrightness) {

    }

    @Override
    public void setR(List<Integer> zones, byte value) throws IOException, InterruptedException {
        send(getMessage(zones, (byte) 8, (byte) 24, value));
    }

    @Override
    public void setG(List<Integer> zones, byte value) throws InterruptedException, IOException {
        send(getMessage(zones, (byte) 8, (byte) 25, value));
    }

    @Override
    public void setB(List<Integer> zones, byte value) throws InterruptedException, IOException {
        send(getMessage(zones, (byte) 8, (byte) 32, value));
    }

    @Override
    public void setRGBBrightness(List<Integer> zones, byte value) throws InterruptedException, IOException {
        send(getMessage(zones, (byte) 8, (byte) 35, value));
    }

    @Override
    public void setRgbHue(List<Integer> zones, float hue) throws InterruptedException, IOException {
        int value = ((97 - Math.round(96.0f * hue)) + 43) % 96;
        if (value == 0) {
            value = 96;
        }

        send(getMessage(zones, (byte) 1, (byte) 1, (byte) value));
    }

    @Override
    public void setW(List<Integer> zones, int value) throws InterruptedException, IOException {
        send(getMessage(zones, (byte) 8, (byte)33, (byte) value));
    }

    @Override
    public void setWBrightness(List<Integer> zones, int value) throws InterruptedException, IOException {
//        send(getMessage(zones, (byte) 8, (byte) 33, (byte) value));
    }

    @Override
    public void resetColor(List<Integer> zones) throws IOException, InterruptedException {
//		this.setRGB(zones, 0, 0, 0);
//		this.setW(zones, 0);
//		this.setBrightness(zones, 7);
    }

    @Override
    public void togglePower(boolean powerState) throws IOException {
        // works
        byte[] data = powerState ? Constants.DATA_ON : Constants.DATA_OFF;
        // os.write(this.getMessage(new ArrayList<Integer>(), data[0], data[1],
        // data[2]));
    }

    @Override
    public void togglePower(List<Integer> zones, boolean powerState) throws IOException, InterruptedException {
        //[85, 43, 20, -64, 2, 1, 2, 18, -87, -64, -86, -86]

        send(getMessage(zones, (byte) 2, (byte) 18, powerState ? (byte) -85 : (byte) -87));
    }

    @Override
    public void toggleColorFader(List<Integer> zones) throws IOException, InterruptedException {
    }

    @Override
    public void speedUpColorFader(List<Integer> zones) throws IOException, InterruptedException {

    }

    @Override
    public void speedDownColorFader(List<Integer> zones) throws IOException, InterruptedException {

    }

    /**
     * set bit for the corresponding zonenumber. If array is empty no bit will be
     * set.
     *
     * @param zones
     * @return
     */
    private byte generateZoneByte(List<Integer> zones) {
        if (zones.size() == 0)
            return 0;

        byte result = 0;
        for (int currentZone : zones) {
            if (currentZone <= 0 || currentZone > 8) {
                continue;
            }
            result = (byte) (result | (1 << currentZone - 1));
        }
        return result;
    }

    /**
     *
     *
     * @param zone
     * @param category
     * @param channel
     * @param value
     * @return
     */
    private byte[] getMessage(int zone, byte category, byte channel, byte value) {
        ArrayList<Integer> zoneArray = new ArrayList<Integer>();
        zoneArray.add(zone);
        return this.getMessage(zoneArray, category, channel, value);
    }

    /**
     * create message for LK35.
     *
     * @param zones
     *            zones will be set in zonebit
     * @param category
     *            see category constants (different remote layouts are grouped in
     *            categories)
     * @param channel
     *            channel or button name
     * @param value
     *            constant or ar range (depends on function of that channel)
     * @return generated message, ready to send
     */
    private byte[] getMessage(List<Integer> zones, byte category, byte channel, byte value) {
        byte[] result = new byte[12];

        // remote identifier
        result[0] = (byte) 85;
        result[1] = (byte) 43;
        result[2] = (byte) 20;
        result[3] = (byte) -64;
        result[4] = (byte) 2;
        // zone
        result[5] = (byte) 1;
        // category - rgb vaules
        result[6] = category;
        // color channel
        result[7] = channel;
        // value
        result[8] = value;
        // checksum
        result[9] = (byte) (result[8] + result[7] + result[6] + result[5] + result[4]);
        // marker bytes
        result[10] = (byte) -86;
        result[11] = (byte) -86;

        return result;
    }

    @Override
    public void saveCurrentColor(List<Integer> zones, int slot) throws IOException, InterruptedException {

    }

    private CountDownLatch lock = new CountDownLatch(0);

    public void send(byte[] bytes1, byte[] bytes2, byte[] bytes3) throws InterruptedException {
//        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(bytes));

        try {
            lock.await();

            if (bytes1 != null) {
                tcpClient.getOutputStream().write(bytes1);
                Thread.sleep(10);
                tcpClient.getOutputStream().flush();
            }
            if (bytes2 != null) {
                tcpClient.getOutputStream().write(bytes2);
                Thread.sleep(10);
                tcpClient.getOutputStream().flush();
            }
            if (bytes3 != null) {
                tcpClient.getOutputStream().write(bytes3);
                Thread.sleep(10);
                tcpClient.getOutputStream().flush();
            }

            readSocket();
            tcpClient.getOutputStream().flush();
        } catch (IOException e) {
            logger.error("Stream write error", e);
            tcpClient.connect();
//            tcpClient.getUpdClient().sendDisableWifi();

            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("Interrupted", e);

            e.printStackTrace();
        }finally {
            lock.countDown();
        }
    }

    private void readSocket() throws IOException {
        try {
            Thread.sleep(SLEEP_AT_END);

            InputStream inputS = tcpClient.getSocket().getInputStream();
            int read;
            while((read = inputS.read()) != -1) {
                System.out.print(read + " ");
            }
            System.out.println();

        } catch (SocketTimeoutException | InterruptedException e) {
        }
    }

    public void send(byte[] bytes) {
//        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(bytes));

        try {
            tcpClient.getOutputStream().write(bytes);
            tcpClient.getOutputStream().flush();

//            readSocket();
        } catch (IOException e) {
            logger.error("Stream write error", e);
            tcpClient.connect();

            e.printStackTrace();
        }
    }
}
