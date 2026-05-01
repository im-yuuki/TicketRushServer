package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {

    USER("user"), PARTNER("partner"), ADMIN("admin");

    @Getter
    private final String value;

    @JsonCreator
    public static Role fromString(String value) {
        for (Role role : values()) {
            if (role.value.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }

}
