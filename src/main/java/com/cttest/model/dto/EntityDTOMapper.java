package com.cttest.model.dto;

import com.cttest.model.entity.Answer;
import com.cttest.model.entity.Center;
import com.cttest.model.entity.Question;
import com.cttest.model.entity.Student;
import com.cttest.model.entity.StudentGroup;
import com.cttest.model.entity.Test;
import com.cttest.model.entity.TestInstance;
import com.cttest.model.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class EntityDTOMapper {

    private EntityDTOMapper() {}

    public static CenterDTO toDTO(Center entity) {
        if (entity == null) return null;
        return new CenterDTO(entity.getId(), entity.getName());
    }

    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;
        return new UserDTO(
                entity.getId(),
                entity.getUsername(),
                entity.isAdmin(),
                Optional.ofNullable(entity.getCenter()).map(Center::getId).orElse(null),
                Optional.ofNullable(entity.getCenter()).map(Center::getName).orElse(null),
                entity.getStudentGroups() != null
                        ? entity.getStudentGroups().stream().map(StudentGroup::getId).collect(java.util.stream.Collectors.toSet())
                        : Collections.emptySet()
        );
    }

    public static TestDTO toDTO(Test entity) {
        if (entity == null) return null;
        return new TestDTO(
                entity.getId(),
                entity.getCenterId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus(),
                entity.getQuestions() != null
                        ? entity.getQuestions().stream().map(EntityDTOMapper::toDTO).toList()
                        : null
        );
    }

    public static StudentGroupDTO toDTO(StudentGroup entity) {
        if (entity == null) return null;
        return new StudentGroupDTO(entity.getId(), entity.getName());
    }

    public static StudentDTO toDTO(Student entity) {
        if (entity == null) return null;
        return new StudentDTO(
                entity.getId(),
                entity.getIdentifier(),
                entity.getName(),
                Optional.ofNullable(entity.getStudentGroup()).map(StudentGroup::getId).orElse(null),
                Optional.ofNullable(entity.getStudentGroup()).map(StudentGroup::getName).orElse(null)
        );
    }

    public static TestInstanceDTO toDTO(TestInstance entity) {
        if (entity == null) return null;
        return new TestInstanceDTO(
                entity.getId(),
                entity.getTestId(),
                null,
                Optional.ofNullable(entity.getStudentGroup()).map(StudentGroup::getId).orElse(null),
                Optional.ofNullable(entity.getStudentGroup()).map(StudentGroup::getName).orElse(null),
                entity.getPin(),
                entity.getAssignedDate(),
                Optional.ofNullable(entity.getAssignedBy()).map(User::getId).orElse(null)
        );
    }

    public static QuestionDTO toDTO(Question entity) {
        if (entity == null) return null;
        return new QuestionDTO(
                entity.getId(),
                entity.getOrder(),
                entity.getQuestion(),
                entity.getImage() != null ? entity.getImage().getImageDataAsBase64() : null,
                entity.getImage() != null ? entity.getImage().getMimeType().getType() : null,
                entity.getAnswersLayout(),
                entity.getAnswers() != null
                        ? entity.getAnswers().stream().map(EntityDTOMapper::toDTO).toList()
                        : null
        );
    }

    public static AnswerDTO toDTO(Answer entity) {
        if (entity == null) return null;
        return new AnswerDTO(
                entity.getId(),
                entity.getOrder(),
                entity.getImage() != null ? entity.getImage().getImageDataAsBase64() : null,
                entity.getImage() != null ? entity.getImage().getMimeType().getType() : null,
                entity.isCorrect()
        );
    }

    public static <T> List<T> toDTOList(List<?> entities, java.util.function.Function<Object, T> mapper) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(mapper).toList();
    }
}
