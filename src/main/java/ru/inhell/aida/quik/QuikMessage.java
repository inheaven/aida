package ru.inhell.aida.quik;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 15:27
 */
public class QuikMessage {
    byte[] errorMessage = new byte[255];
    LongByReference code = new LongByReference();
    NativeLong result = new NativeLong();

    public byte[] getErrorMessage() {
        return errorMessage;
    }

    public LongByReference getCode() {
        return code;
    }

    public int getSize() {
        return errorMessage.length;
    }

    public NativeLong getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "QuikMessage{" +
                "errorMessage=" + Native.toString(errorMessage, "cp1251") +
                ", code=" + code.getValue() +
                ", result=" + result +
                '}';
    }
}
