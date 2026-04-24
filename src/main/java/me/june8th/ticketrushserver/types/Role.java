package me.june8th.ticketrushserver.types;

import jakarta.annotation.Nullable;
import lombok.Getter;

public enum Role {

    USER("user"), PARTNER("partner"), ADMIN("admin");

    @Getter
    private final String value;

    Role(String value) {
        this.value = value;
    }

    @Nullable
    public static Role fromString(String value) {
        for (Role role : values()) {
            if (role.value.equalsIgnoreCase(value)) return role;
        }
        return null;
    }

}
