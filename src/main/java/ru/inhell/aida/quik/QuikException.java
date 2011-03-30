package ru.inhell.aida.quik;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 15:57
 */
public class QuikException extends Exception{
    private QuikMessage quikMessage;

    public QuikException() {
    }

    public QuikException(QuikMessage quikMessage) {
        this.quikMessage = quikMessage;
    }

    public QuikMessage getQuikMessage() {
        return quikMessage;
    }

    @Override
    public String getMessage() {
        return quikMessage.result.intValue() + ": " + quikMessage.code.getValue() + ": " +quikMessage.message;
    }
}
