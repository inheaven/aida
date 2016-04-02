package ru.inheaven.aida.happy.trading.entity;

/**
 * inheaven on 13.03.2016.
 */
public enum LevelParameter {
    CANCEL,
    CANCEL_RATE,
    LOT,
    LOT_TYPE, //fixed, uniform, gauss
    SPREAD,
    SPREAD_TYPE, // fixed, percent, volatility
    SIDE_SPREAD,
    SIDE_SPREAD_TYPE, //fixed, percent
    SIZE,
    BALANCE,
    BALANCE_TYPE, //disabled, total
    BALANCE_RATE,
    VOLATILITY,
    VOLATILITY_SIZE
}
