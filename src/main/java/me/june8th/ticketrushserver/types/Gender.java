package me.june8th.ticketrushserver.types;

import lombok.Getter;

import javax.annotation.Nullable;

public enum Gender {

    MALE("male"), FEMALE("female"), OTHER("other");

    @Getter
    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @Nullable
    public static Gender fromString(String value) {
        for (Gender gender : values()) {
            if (gender.value.equalsIgnoreCase(value)) return gender;
        }
        return null;
    }

}
