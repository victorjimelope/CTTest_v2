package com.cttest.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenderType {

    UNKNOWN(0, "Unknown"),
    MALE(1, "Male"),
    FEMALE(2, "Female"),
    NOT_APPLICABLE(9, "Not applicable");

    private final Integer id;
    private final String title;

    public static GenderType getById(Integer id) {
        if (id == null) return null;
        for (GenderType item : values()) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }

}
