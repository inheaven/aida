package ru.inheaven.aida.coin.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author inheaven on 13.02.2015 3:24.
 */
@Converter(autoApply = true)
public class ExchangeTypeConverter implements AttributeConverter<ExchangeType, String> {
    @Override
    public String convertToDatabaseColumn(ExchangeType exchangeType) {
        return exchangeType.name();
    }

    @Override
    public ExchangeType convertToEntityAttribute(String s) {
        return ExchangeType.valueOf(s);
    }
}