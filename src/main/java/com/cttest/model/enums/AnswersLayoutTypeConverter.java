package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AnswersLayoutTypeConverter implements AttributeConverter<AnswersLayoutType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AnswersLayoutType attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public AnswersLayoutType convertToEntityAttribute(Integer dbData) {
        return AnswersLayoutType.getById(dbData);
    }

}
