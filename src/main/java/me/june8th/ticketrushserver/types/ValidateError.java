package me.june8th.ticketrushserver.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValidateError {

    NAME_TOO_SHORT("name_too_short", "Name is too short"),
    NAME_TOO_LONG("name_too_long", "Name is too long"),
    EMAIL_INVALID("email_invalid", "Invalid email format"),
    PASSWORD_TOO_SHORT("password_too_short", "Password is too short"),
    PASSWORD_MISSING_CASES("password_missing_cases", "Password must contain at least one uppercase letter, one lowercase letter, and one digit"),
    BIRTHDATE_INVALID("birthdate_invalid", "Invalid birth date"),
    DATE_NOT_IN_PAST("date_not_in_past", "Date must be in the past"),
    DATE_NOT_IN_FUTURE("date_not_in_future", "Date must be in the future"),

    MISSING_REQUIRED_FIELD("missing_required_field", "Missing required field"),
    NONE("none", "");

    @Getter
    private final String value;

    @Getter
    private final String message;

}
