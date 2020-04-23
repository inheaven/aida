package org.sunricher.wifi.api;

import java.io.Serializable;
import java.util.ArrayList;

public class WifiCommand implements Serializable {
    private byte appIdFirst_1;
    private byte appIdLast_3;
    private byte appIdSecond_2;
    private byte dataType_6;
    private ArrayList<byte[]> datas = new ArrayList();
    private byte deviceType_4 = (byte) 2;
    private byte endFirst_10 = (byte) -86;
    private byte endLast_11 = (byte) -86;
    private byte head_0 = (byte) 85;
    private byte key_7;
    private byte selectArea_5;
    private byte value_8;

    private WifiCommand() {
        this.appIdFirst_1 = (byte) ((1 >> 16) & 255);
        this.appIdSecond_2 = (byte) ((1 >> 8) & 255);
        this.appIdLast_3 = (byte) (1 & 255);
        this.selectArea_5 = (byte) 1;
    }

    public ArrayList<byte[]> getDatas() {
        return this.datas;
    }



    public static WifiCommand powerOnAllDevices() {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 2;
        cmd.key_7 = (byte) 18;
        cmd.value_8 = (byte) -85;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand powerOffAllDevices() {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 2;
        cmd.key_7 = (byte) 18;
        cmd.value_8 = (byte) -87;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }


    public static WifiCommand rgbHue(float hue) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 1;
        cmd.key_7 = (byte) 1;
        int value = ((97 - Math.round(96.0f * hue)) + 43) % 96;
        if (value == 0) {
            value = 96;
        }
        cmd.value_8 = (byte) value;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbBrightness(float brightness) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 76;
        int value = Math.round(64.0f * brightness);
        if (value <= 0) {
            value = 1;
        }
        cmd.value_8 = (byte) value;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbWhite(float white) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 75;
        cmd.value_8 = (byte) Math.round(255.0f * white);
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbStartRun(byte mode) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 2;
        cmd.key_7 = (byte) 78;
        int value = mode + 20;
        if (value < 21) {
            value = 21;
        } else if (value > 30) {
            value = 30;
        }
        cmd.value_8 = (byte) value;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbRunSpeed(float speed) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 34;
        cmd.value_8 = (byte) Math.round(10.0f * speed);
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbRed(float red) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 72;
        cmd.value_8 = (byte) Math.round(255.0f * red);
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbGreen(float green) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 73;
        cmd.value_8 = (byte) Math.round(255.0f * green);
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand rgbBlue(float blue) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 74;
        cmd.value_8 = (byte) Math.round(255.0f * blue);
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand cctHue(float hue) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 54;
        cmd.value_8 = (byte) Math.round(32.0f * hue);
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand cctBrightness(float brightness) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 51;
        int value = Math.round(255.0f * brightness);
        if (value <= 0) {
            value = 1;
        }
        cmd.value_8 = (byte) value;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    public static WifiCommand dimHue(float hue) {
        WifiCommand cmd = new WifiCommand();
        cmd.dataType_6 = (byte) 8;
        cmd.key_7 = (byte) 56;
        int value = Math.round(255.0f * hue);
        if (value <= 0) {
            value = 1;
        }
        cmd.value_8 = (byte) value;
        cmd.getDatas().add(cmd.bytesBySelf());
        return cmd;
    }

    private byte[] bytesBySelf() {
        byte sum_9 = getSum_9();
        return new byte[]{this.head_0, this.appIdFirst_1, this.appIdSecond_2, this.appIdLast_3, this.deviceType_4, this.selectArea_5, this.dataType_6, this.key_7, this.value_8, sum_9, this.endFirst_10, this.endLast_11};
    }

    private byte getSum_9() {
        return (byte) ((((this.deviceType_4 + this.selectArea_5) + this.dataType_6) + this.key_7) + this.value_8);
    }
}
