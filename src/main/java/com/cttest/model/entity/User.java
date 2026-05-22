package com.cttest.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements Serializable {

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @Column(name = "admin")
    private boolean admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Center center;

    @ManyToMany
    @JoinTable(
            name = "rel_user_student_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "student_group_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<StudentGroup> studentGroups = new HashSet<>();

    @Transient
    private String newPassword;

    public User(Integer id) {
        this.id = id;
    }

    public String getName() {
        return username;
    }

    public String getStudentGroupsAsString() {
        if (studentGroups == null || studentGroups.isEmpty()) {
            return null;
        }
        return studentGroups.stream()
                .map(StudentGroup::getName)
                .collect(Collectors.joining(", "));
    }

}
