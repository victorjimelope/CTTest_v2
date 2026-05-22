package com.cttest.model.dto;

import com.cttest.model.entity.Answer;
import com.cttest.model.entity.Question;
import com.cttest.model.entity.Test;

import java.util.List;

public final class TestDataMapper {

    private TestDataMapper() {}

    public static AnswerDataDTO toAnswerDTO(Answer answer) {
        return new AnswerDataDTO(
                answer.getId(),
                answer.getOrder(),
                answer.getImage() != null ? answer.getImage().getImageDataAsBase64() : null
        );
    }

    public static QuestionDataDTO toQuestionDTO(Question question) {
        List<AnswerDataDTO> answerDTOs = question.getAnswers().stream()
                .map(TestDataMapper::toAnswerDTO)
                .toList();
        return new QuestionDataDTO(
                question.getId(),
                question.getOrder(),
                question.getQuestion(),
                question.getImage() != null ? question.getImage().getImageDataAsBase64() : null,
                question.getAnswersLayout() != null ? question.getAnswersLayout().name() : null,
                answerDTOs
        );
    }

    public static TestDataDTO toTestDataDTO(Test test) {
        List<QuestionDataDTO> questionDTOs = test.getQuestions().stream()
                .map(TestDataMapper::toQuestionDTO)
                .toList();
        return new TestDataDTO(
                test.getId(),
                test.getName(),
                questionDTOs,
                questionDTOs.size()
        );
    }

}
