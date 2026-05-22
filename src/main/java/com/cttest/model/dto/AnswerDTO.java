package com.cttest.model.dto;

public record AnswerDTO(
        Integer id,
        Integer order,
        String imageData,
        String mimeType,
        boolean correct) {
}
