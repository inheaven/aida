package ru.inhell.aida.quik;

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
    String resultMessage = "";
    Integer resultMessageSize = 0;
    LongByReference extendedErrorCode = new LongByReference();
    String errorMessage = "";
    Integer errorMessageSize = 0;

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

    public String getResultMessage() {
        return resultMessage;
    }

    public Integer getResultMessageSize() {
        return resultMessageSize;
    }

    public LongByReference getExtendedErrorCode() {
        return extendedErrorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Integer getErrorMessageSize() {
        return errorMessageSize;
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
                ", resultMessage='" + resultMessage + '\'' +
                ", resultMessageSize=" + resultMessageSize +
                ", extendedErrorCode=" + extendedErrorCode.getValue() +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorMessageSize=" + errorMessageSize +
                ", result=" + result.intValue() +
                '}';
    }
}
