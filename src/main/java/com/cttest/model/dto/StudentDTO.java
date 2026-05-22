package com.cttest.model.dto;

public record StudentDTO(
        Integer id,
        Integer identifier,
        String name,
        Integer studentGroupId,
        String studentGroupName) {
}
