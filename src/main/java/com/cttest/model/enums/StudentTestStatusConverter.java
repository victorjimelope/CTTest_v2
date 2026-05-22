package com.cttest.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StudentTestStatusConverter implements AttributeConverter<StudentTestStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(StudentTestStatus attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public StudentTestStatus convertToEntityAttribute(Integer dbData) {
        return StudentTestStatus.getById(dbData);
    }

}
