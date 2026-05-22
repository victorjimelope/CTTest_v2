package com.cttest.model.entity;

import com.cttest.model.enums.TestStatus;
import com.cttest.model.enums.TestType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Test extends BaseEntity implements Serializable {

    @Column(name = "center_id")
    private Integer centerId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type_id")
    private TestType type;

    @Column(name = "status_id")
    private TestStatus status;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"test", "hibernateLazyInitializer", "handler"})
    private List<Question> questions = new ArrayList<>();

    public boolean isActive() {
        return status != null && status.equals(TestStatus.ACTIVE);
    }

}
