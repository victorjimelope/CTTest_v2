package com.cttest.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "add_date")
    protected LocalDateTime addDate;

    @Column(name = "add_user")
    protected Integer addUser;

    @Column(name = "mod_date")
    protected LocalDateTime modDate;

    @Column(name = "mod_user")
    protected Integer modUser;

}
