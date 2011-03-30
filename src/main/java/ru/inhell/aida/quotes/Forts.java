package ru.inhell.aida.quotes;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 * Date: 31.03.11 2:04
 */
public enum Forts {
    GAZP("GZM1"), LKOH("LKM1"), SBER03("SRM1");

    private String fortsSymbol;

    Forts(String fortsSymbol) {
        this.fortsSymbol = fortsSymbol;
    }

    public String getFortsSymbol() {
        return fortsSymbol;
    }
}
