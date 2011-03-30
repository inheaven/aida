package ru.inhell.aida.quik;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 15:27
 */
public class QuikMessage {
    String message = "";
    LongByReference code = new LongByReference();
    Integer size = 256;
    NativeLong result;

    public String getMessage() {
        return message;
    }

    public LongByReference getCode() {
        return code;
    }

    public Integer getSize() {
        return size;
    }

    public NativeLong getResult() {
        return result;
    }
}
