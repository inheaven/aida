package ru.inhell.aida.quik;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.gui.Aida;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 15:07
 */
public class QuikService {
    private static final Logger log = LoggerFactory.getLogger(QuikService.class);

    public static enum OPERATION{
        BUY("B"), SELL("S");

        private String code;

        OPERATION(String code) {
            this.code = code;
        }
    }

    public void connect(String quikDir) throws QuikException {
        QuikMessage qm = new QuikMessage();

        try {
            qm.result =  Trans2Quik.INSTANCE.TRANS2QUIK_CONNECT(quikDir, qm.code, qm.errorMessage, qm.getSize());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (qm.result.intValue() != Trans2Quik.TRANS2QUIK_SUCCESS){
            throw new QuikException(qm);
        }

        log.info(qm.toString());
    }

    public void checkConnection() throws QuikException {
        QuikMessage qm = new QuikMessage();

        qm.result  = Trans2Quik.INSTANCE.TRANS2QUIK_IS_QUIK_CONNECTED(qm.code, qm.errorMessage, qm.getSize());

        if (qm.result.intValue() != Trans2Quik.TRANS2QUIK_QUIK_CONNECTED){
            throw new QuikException(qm);
        }
    }

    public void disconnect() throws QuikException {
        QuikMessage qm = new QuikMessage();

        qm.result = Trans2Quik.INSTANCE.TRANS2QUIK_DISCONNECT(qm.code, qm.errorMessage, qm.getSize());

        if (qm.result.intValue() != Trans2Quik.TRANS2QUIK_SUCCESS){
            throw new QuikException(qm);
        }
    }

    public QuikTransaction buyFutures(long transId, String symbol, int price, int quantity) throws QuikTransactionException {
        return newOrder(transId, ru.inhell.aida.Aida.getAccount(), "SPBFUT", symbol, OPERATION.BUY, price, quantity);
    }

    public QuikTransaction sellFutures(long transId, String symbol, int price, int quantity) throws QuikTransactionException {
        return newOrder(transId, ru.inhell.aida.Aida.getAccount(), "SPBFUT", symbol, OPERATION.SELL, price, quantity);
    }

    /**
     *
     * @param transId
     * @param account
     * @param classCode
     * @param symbol
     * @param operation
     * @param price
     * @param quantity
     * @return Результат выполнения операции. Может принимать одно из следующих значений:
        «0» - транзакция отправлена серверу,
        «1» - транзакция получена на сервер QUIK от клиента,
        «2» - ошибка при передаче транзакции в торговую систему, поскольку отсутствует подключение шлюза ММВБ,
            повторно транзакция не отправляется,
        «3» - транзакция выполнена,
        «4» - транзакция не выполнена торговой системой, код ошибки торговой системы будет указан в поле «DESCRIPTION»,
        «5» - транзакция не прошла проверку сервера QUIK по каким-либо критериям. Например, проверку на наличие прав
            у пользователя на отправку транзакции данного типа,
        «6» - транзакция не прошла проверку лимитов сервера QUIK,
        «7» - транзакция клиента, работающего с подтверждением, подтверждена менеджером фирмы,
        «8» - транзакция клиента, работающего с подтверждением, не подтверждена менеджером фирмы,
        «9» - транзакция клиента, работающего с подтверждением, снята менеджером фирмы,
        «10» - транзакция не поддерживается торговой системой. К примеру, попытка отправить «ACTION = MOVE_ORDERS»
            на ММВБ,
        «11» - транзакция не прошла проверку правильности электронной подписи. К примеру, если ключи, зарегистрированные
            на сервере, не соответствуют подписи отправленной транзакции
     * @throws QuikTransactionException
     */
    private QuikTransaction newOrder(long transId, String account, String classCode, String symbol, OPERATION operation,
                                     int price, int quantity) throws QuikTransactionException {
        QuikTransaction qt = new QuikTransaction("ACTION=NEW_ORDER; ACCOUNT=" + account + "; TRANS_ID=" +transId +
                "; CLASSCODE=" + classCode + "; SECCODE=" + symbol + "; OPERATION=" + operation.code +
                "; PRICE=" + price + "; QUANTITY=" + quantity);

        try {
            qt.result = Trans2Quik.INSTANCE.TRANS2QUIK_SEND_SYNC_TRANSACTION(qt.transaction, qt.replyCode, qt.transId,
                    qt.orderNum, qt.resultMessage, qt.getResultMessageSize(), qt.extendedErrorCode, qt.errorMessage,
                    qt.getErrorMessageSize());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (qt.result.intValue() != Trans2Quik.TRANS2QUIK_SUCCESS || qt.replyCode.getValue() != 3){
            throw new QuikTransactionException(qt);
        }

        return qt;
    }
}
