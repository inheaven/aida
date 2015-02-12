package ru.inheaven.aida.coin.entity;

import javax.persistence.AttributeConverter;

/**
 * @author inheaven on 13.02.2015 3:31.
 */
public class OrderTypeConverter implements AttributeConverter<OrderType, String> {
    @Override
    public String convertToDatabaseColumn(OrderType orderType) {
        return orderType.name();
    }

    @Override
    public OrderType convertToEntityAttribute(String s) {
        return OrderType.valueOf(s);
    }
}
