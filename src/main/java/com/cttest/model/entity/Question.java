package com.cttest.model.entity;

import com.cttest.model.enums.AnswersLayoutType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"test", "answers"})
public class Question extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"questions", "hibernateLazyInitializer", "handler"})
    private Test test;

    @Column(name = "order_")
    private Integer order;

    @Column(name = "question")
    private String question;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id", unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Image image;

    @Column(name = "layout_type_id", nullable = false)
    private AnswersLayoutType answersLayout;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"question", "hibernateLazyInitializer", "handler"})
    private List<Answer> answers = new ArrayList<>();

    @Formula("(SELECT COUNT(*) FROM student_answer sa WHERE sa.question_id = id)")
    private Integer countTotal;

    @Transient
    private boolean selected;

    @Transient
    private Integer countCorrect;

    @Transient
    private Integer countWrong;

    public Integer getCountTotal() {
        if (countTotal == null) {
            countTotal = 0;
        }
        return countTotal;
    }

    public Integer getCountCorrect() {
        if (countCorrect == null) {
            loadCountCorrect();
        }
        return countCorrect;
    }

    private void loadCountCorrect() {
        countCorrect = 0;
        if (answers != null) {
            for (Answer ans : answers) {
                if (ans.isCorrect()) {
                    countCorrect = ans.getCountTotal();
                    return;
                }
            }
        }
    }

    public Integer getCountWrong() {
        if (countWrong == null) {
            loadCountWrong();
        }
        return countWrong;
    }

    private void loadCountWrong() {
        countWrong = getCountTotal() - getCountCorrect();
    }

    public Integer getCorrectAnswerId() {
        if (answers != null) {
            for (Answer answer : answers) {
                if (answer.isCorrect()) {
                    return answer.getId();
                }
            }
        }
        return null;
    }

    public Question clone() {
        Question clone = new Question();
        clone.setTest(test);
        clone.setOrder(order);
        clone.setQuestion(question);
        clone.setImage(image != null ? image.clone() : null);
        clone.setAnswersLayout(answersLayout);
        clone.setAnswers(new ArrayList<>());
        for (Answer ans : answers) {
            Answer cloneAns = ans.clone();
            cloneAns.setQuestion(clone);
            clone.getAnswers().add(cloneAns);
        }
        return clone;
    }

}
