package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TestTypeConverter implements AttributeConverter<TestType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TestType attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public TestType convertToEntityAttribute(Integer dbData) {
        return TestType.getById(dbData);
    }

}
