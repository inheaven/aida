package ru.inhell.aida.quik;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.03.11 16:15
 */
public class QuikTransaction {
    String transaction;

    LongByReference replyCode = new LongByReference();
    IntByReference transId = new IntByReference();
    DoubleByReference orderNum = new DoubleByReference();
    byte[] resultMessage = new byte[255];
    LongByReference extendedErrorCode = new LongByReference();
    byte[] errorMessage = new byte[255];

    NativeLong result;

    public QuikTransaction() {
    }

    public QuikTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getTransaction() {
        return transaction;
    }

    public LongByReference getReplyCode() {
        return replyCode;
    }

    public IntByReference getTransId() {
        return transId;
    }

    public DoubleByReference getOrderNum() {
        return orderNum;
    }

    public byte[] getResultMessage() {
        return resultMessage;
    }

    public int getResultMessageSize() {
        return resultMessage.length;
    }

    public LongByReference getExtendedErrorCode() {
        return extendedErrorCode;
    }

    public byte[] getErrorMessage() {
        return errorMessage;
    }

    public int getErrorMessageSize() {
        return errorMessage.length;
    }

    public NativeLong getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "QuikTransaction{" +
                "transaction='" + transaction + '\'' +
                ", replyCode=" + replyCode.getValue() +
                ", transId=" + transId.getValue() +
                ", orderNum=" + (long)orderNum.getValue() +
                ", resultMessage='" + Native.toString(resultMessage, "cp1251") + '\'' +
                ", extendedErrorCode=" + extendedErrorCode.getValue() +
                ", errorMessage='" + Native.toString(errorMessage, "cp1251") + '\'' +
                ", result=" + result.intValue() +
                '}';
    }
}
