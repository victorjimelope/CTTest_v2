package com.cttest.model.entity;

import com.cttest.model.enums.StudentTestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cttest.util.Constants.DATE_TIME_FORMAT;

@Entity
@Table(name = "student_test")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentTest extends BaseEntity implements Serializable {

    @Column(name = "test_instance_id")
    private Integer testInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @Column(name = "status_id", nullable = false)
    private StudentTestStatus status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "finish_date")
    private LocalDateTime finishDate;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "comment")
    private String comment;

    public boolean isAssigned() {
        return status != null && status.equals(StudentTestStatus.ASSIGNED);
    }

    public boolean isInProgress() {
        return status != null && status.equals(StudentTestStatus.IN_PROGRESS);
    }

    public boolean isClosed() {
        return status != null && (status.equals(StudentTestStatus.FINISHED)
                || status.equals(StudentTestStatus.NOT_DONE));
    }

    @Transient
    public String getStartDateAsString() {
        return startDate != null ? startDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) : null;
    }

    @Transient
    public String getFinishDateAsString() {
        return finishDate != null ? finishDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) : null;
    }

}
