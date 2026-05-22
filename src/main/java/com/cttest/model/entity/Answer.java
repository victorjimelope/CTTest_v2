package com.cttest.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.io.Serializable;

@Entity
@Table(name = "answer")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "question")
public class Answer extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"answers", "hibernateLazyInitializer", "handler"})
    private Question question;

    @Column(name = "order_")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id", unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Image image;

    @Column(name = "correct")
    private boolean correct;

    @Formula("(SELECT COUNT(*) FROM student_answer sa WHERE sa.answer_id = id)")
    private Integer countTotal;

    @Transient
    private boolean selected;

    public Integer getCountTotal() {
        if (countTotal == null) {
            countTotal = 0;
        }
        return countTotal;
    }

    public Answer clone() {
        Answer clone = new Answer();
        clone.setQuestion(question);
        clone.setOrder(order);
        clone.setImage(image != null ? image.clone() : null);
        clone.setCorrect(correct);
        return clone;
    }

}
