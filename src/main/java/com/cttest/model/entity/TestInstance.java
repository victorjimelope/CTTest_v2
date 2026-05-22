package com.cttest.model.entity;

import com.cttest.model.enums.TestInstanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "test_instance")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestInstance extends BaseEntity implements Serializable {

    @Column(name = "test_id")
    private Integer testId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_group_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StudentGroup studentGroup;

    @Column(name = "status_id")
    private TestInstanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User assignedBy;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "pin")
    private Integer pin;

    public boolean isClosed() {
        return status != null && status.equals(TestInstanceStatus.CLOSED);
    }

}
