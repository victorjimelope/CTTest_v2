package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MimeTypeConverter implements AttributeConverter<MimeType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MimeType attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public MimeType convertToEntityAttribute(Integer dbData) {
        return MimeType.getById(dbData);
    }

}
