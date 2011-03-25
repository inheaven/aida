package ru.inhell.aida.quik;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 15:27
 */
public class QuikMessage {
    private String message = "";
    private LongByReference code = new LongByReference();
    private Integer size = -1;

    public QuikMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LongByReference getCode() {
        return code;
    }

    public void setCode(LongByReference code) {
        this.code = code;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
