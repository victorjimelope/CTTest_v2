package com.cttest.model.dto;

import com.cttest.model.enums.AnswersLayoutType;

import java.util.List;

public record QuestionDTO(
        Integer id,
        Integer order,
        String question,
        String imageData,
        String mimeType,
        AnswersLayoutType answersLayout,
        List<AnswerDTO> answers) {
}
