package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AccountType {

    USER("user"), INSPECTOR("inspector"), ORGANIZATION("organization"), ADMINISTRATOR("administrator");

    private final String value;

    @JsonCreator
    public static AccountType fromString(String value) {
        for (AccountType type : values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid JWT token type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }

}
