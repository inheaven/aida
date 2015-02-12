package ru.inheaven.aida.coin.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author inheaven on 13.02.2015 3:30.
 */
@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus orderStatus) {
        return orderStatus.name();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String s) {
        return OrderStatus.valueOf(s);
    }
}
