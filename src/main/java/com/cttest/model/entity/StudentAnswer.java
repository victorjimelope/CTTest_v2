package com.cttest.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_answer", uniqueConstraints = @UniqueConstraint(columnNames = {"student_test_id", "question_id"}))
@Getter
@Setter
@NoArgsConstructor
public class StudentAnswer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_test_id")
    private Integer studentTestId;

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "answer_id")
    private Integer answerId;

    @Column(name = "add_date")
    private LocalDateTime addDate;

    @Column(name = "mod_date")
    private LocalDateTime modDate;

}
