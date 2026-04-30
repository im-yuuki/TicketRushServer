package me.june8th.ticketrushserver.types;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Gender {

    MALE("male"), FEMALE("female"), OTHER("other");

    @Getter
    private final String value;

    @Nullable
    public static Gender fromString(String value) {
        for (Gender gender : values()) {
            if (gender.value.equalsIgnoreCase(value)) return gender;
        }
        return null;
    }

}
