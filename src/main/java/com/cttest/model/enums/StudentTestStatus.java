package com.cttest.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentTestStatus {

    ASSIGNED(1, "Assigned", "secondary"),
    IN_PROGRESS(2, "In progress", "warning"),
    FINISHED(3, "Finished", "success"),
    NOT_DONE(4, "Not done", "danger");

    private final Integer id;
    private final String title;
    private final String color;

    public static StudentTestStatus getById(Integer id) {
        if (id == null) return null;
        for (StudentTestStatus item : values()) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }

}
