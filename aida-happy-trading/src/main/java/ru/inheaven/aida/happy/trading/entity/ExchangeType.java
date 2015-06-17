package ru.inheaven.aida.happy.trading.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.2014 4:12
 */
public enum ExchangeType {
    CEXIO("CX"), CRYPTSY("CS"), BITTREX("BT"), BTCE("BE"), BTER("BR"), BITFINEX("BF"), OKCOIN_SPOT("OK"), OKCOIN_FUTURES("OF");

    private String shortName;

    ExchangeType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}
