package com.cttest.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestInstanceStatus {

    IN_PROGRESS(1, "In progress", "warning"),
    CLOSED(2, "Closed", "danger");

    private final Integer id;
    private final String title;
    private final String color;

    public static TestInstanceStatus getById(Integer id) {
        if (id == null) return null;
        for (TestInstanceStatus item : values()) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }

}
