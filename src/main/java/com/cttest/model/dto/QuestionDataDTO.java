package com.cttest.model.dto;

import java.util.List;

public record QuestionDataDTO(
        Integer id,
        Integer order,
        String question,
        String imageData,
        String answersLayout,
        List<AnswerDataDTO> answers
) {}
