package ru.inhell.aida.quik;

import com.sun.jna.NativeLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 15:07
 */
public class QuikService {
    private static final Logger log = LoggerFactory.getLogger(QuikService.class);

    public static final String QUIK_DIR = "C:\\Anatoly\\QUIK";

    public void connect() throws QuikException {
        QuikMessage qm = new QuikMessage();

        NativeLong res =  Trans2Quik.INSTANCE.TRANS2QUIK_CONNECT(QUIK_DIR, qm.getCode(), qm.getMessage(), qm.getSize());

        if (res.intValue() != Trans2Quik.TRANS2QUIK_SUCCESS){
            throw new QuikException(qm);
        }
    }

    public void checkConnection() throws QuikException {
        QuikMessage qm = new QuikMessage();

        NativeLong res = Trans2Quik.INSTANCE.TRANS2QUIK_IS_QUIK_CONNECTED(qm.getCode(), qm.getMessage(), qm.getSize());

        if (res.intValue() != Trans2Quik.TRANS2QUIK_QUIK_CONNECTED){
            throw new QuikException(qm);
        }
    }

    public void disconnect() throws QuikException {
        QuikMessage qm = new QuikMessage();

        NativeLong res = Trans2Quik.INSTANCE.TRANS2QUIK_DISCONNECT(qm.getCode(), qm.getMessage(), qm.getSize());

        if (res.intValue() != Trans2Quik.TRANS2QUIK_SUCCESS){
            throw new QuikException(qm);
        }
    }

    public void buyFutures(String symbol, int quantity, float price){

    }

    public void sellFutures(String symbol, int quantity, float price){

    }
}
