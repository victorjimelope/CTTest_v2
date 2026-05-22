package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderTypeConverter implements AttributeConverter<GenderType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(GenderType attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public GenderType convertToEntityAttribute(Integer dbData) {
        return GenderType.getById(dbData);
    }

}
