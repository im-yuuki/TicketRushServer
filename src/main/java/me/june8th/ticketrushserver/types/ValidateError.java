package me.june8th.ticketrushserver.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValidateError {

    TOO_SHORT_NAME("Name is too short"),
    TOO_LONG_NAME("Name is too long"),
    INVALID_EMAIL("Invalid email format"),
    TOO_SHORT_PASSWORD("Password is too short"),
    MISSING_UPPERCASE_PASSWORD("Password must contain at least one uppercase letter"),
    MISSING_LOWERCASE_PASSWORD("Password must contain at least one lowercase letter"),
    MISSING_DIGIT_PASSWORD("Password must contain at least one digit"),
    MISSING_FIELD("Missing required field"),
    NONE("");

    @Getter
    private final String value;

}
