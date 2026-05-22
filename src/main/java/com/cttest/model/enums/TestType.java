package com.cttest.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestType {

    PUBLIC(1, "Public", "success"),
    PRIVATE(2, "Private", "primary");

    private final Integer id;
    private final String title;
    private final String color;

    public static TestType getById(Integer id) {
        if (id == null) return null;
        for (TestType item : values()) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }

}
