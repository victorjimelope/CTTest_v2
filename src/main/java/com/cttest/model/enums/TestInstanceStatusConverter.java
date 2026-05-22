package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TestInstanceStatusConverter implements AttributeConverter<TestInstanceStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TestInstanceStatus attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public TestInstanceStatus convertToEntityAttribute(Integer dbData) {
        return TestInstanceStatus.getById(dbData);
    }

}
