package ru.inhell.aida.light.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 03.05.12 0:06
 */
public interface Yocto extends Library {
    Yocto INSTANCE = (Yocto) Native.loadLibrary("yocto", Yocto.class);

    String yGetAPIVersion();

    Integer yRegisterHub(String s, String errmsg);
}
