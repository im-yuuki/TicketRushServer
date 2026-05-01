package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Gender {

    MALE("male"), FEMALE("female"), OTHER("other");

    @Getter
    private final String value;

    @JsonCreator
    public static Gender fromString(String value) {
        for (Gender gender : values()) {
            if (gender.value.equalsIgnoreCase(value) || gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + value);
    }

    @Override
    public String toString() {
        return value;
    }


}
