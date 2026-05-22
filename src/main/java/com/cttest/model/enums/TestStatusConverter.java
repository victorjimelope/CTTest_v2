package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TestStatusConverter implements AttributeConverter<TestStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TestStatus attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public TestStatus convertToEntityAttribute(Integer dbData) {
        return TestStatus.getById(dbData);
    }

}
