package com.cttest.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestStatus {

    DRAFT(1, "Draft", "warning"),
    ACTIVE(2, "Active", "success"),
    LOCKED(3, "Locked", "danger");

    private final Integer id;
    private final String title;
    private final String color;

    public static TestStatus getById(Integer id) {
        if (id == null) return null;
        for (TestStatus item : values()) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }

}
