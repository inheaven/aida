package ru.inheaven.aida.coin.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author inheaven on 13.02.2015 3:27.
 */
@Converter(autoApply = true)
public class TraderTypeConverter implements AttributeConverter<TraderType, String> {
    @Override
    public String convertToDatabaseColumn(TraderType traderType) {
        return traderType.name();
    }

    @Override
    public TraderType convertToEntityAttribute(String s) {
        return TraderType.valueOf(s);
    }
}
