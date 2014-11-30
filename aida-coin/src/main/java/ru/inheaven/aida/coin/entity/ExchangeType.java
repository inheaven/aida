package ru.inheaven.aida.coin.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.2014 4:12
 */
public enum ExchangeType {
    CEXIO("CX"), CRYPTSY("CR"), BITTREX("BT"), BTCE("BE"), BTER("BR"), BITFINEX("BF"), OKCOIN("OK");

    private String shortName;

    ExchangeType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}
