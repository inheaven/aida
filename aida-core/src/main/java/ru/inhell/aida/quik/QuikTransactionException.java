package ru.inhell.aida.quik;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.03.11 16:14
 */
public class QuikTransactionException extends Exception{
    private QuikTransaction quikTransaction;

    public QuikTransactionException(QuikTransaction quikTransaction) {
        this.quikTransaction = quikTransaction;
    }

    @Override
    public String getMessage() {
        return quikTransaction.toString();
    }

    public QuikTransaction getQuikTransaction() {
        return quikTransaction;
    }
}
