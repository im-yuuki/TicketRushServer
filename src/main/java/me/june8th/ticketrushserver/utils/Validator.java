package me.june8th.ticketrushserver.utils;

import lombok.Getter;
import me.june8th.ticketrushserver.types.ValidateError;

public class Validator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    @Getter
    private ValidateError error = ValidateError.NONE;

    public Validator validateName(String name) {
        if (error != ValidateError.NONE) return this;
        if (name == null) {
            error = ValidateError.MISSING_FIELD;
            return this;
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 3) error = ValidateError.TOO_SHORT_NAME;
        else if (trimmedName.length() > 50) error = ValidateError.TOO_LONG_NAME;
        return this;
    }

    public Validator validateEmail(String email) {
        if (error != ValidateError.NONE) return this;
        if (email == null) {
            error = ValidateError.MISSING_FIELD;
            return this;
        }
        String trimmedEmail = email.trim();
        if (trimmedEmail.length() > 100) {
            error = ValidateError.INVALID_EMAIL;
            return this;
        }
        if (!trimmedEmail.matches(EMAIL_REGEX)) error = ValidateError.INVALID_EMAIL;
        return this;
    }

    public Validator validatePassword(String password) {
        if (error != ValidateError.NONE) return this;
        if (password == null) {
            error = ValidateError.MISSING_FIELD;
            return this;
        } else if (password.length() < 8) {
            error = ValidateError.TOO_SHORT_PASSWORD;
            return this;
        }
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUppercase) error = ValidateError.MISSING_UPPERCASE_PASSWORD;
        else if (!hasLowercase) error = ValidateError.MISSING_LOWERCASE_PASSWORD;
        else if (!hasDigit) error = ValidateError.MISSING_DIGIT_PASSWORD;
        return this;
    }

    public boolean valid() {
        return error == ValidateError.NONE;
    }

    public void throwIfInvalid() throws IllegalArgumentException {
        if (error != ValidateError.NONE) {
            throw new IllegalArgumentException(error.getValue());
        }
    }

    public static Validator create() {
        return new Validator();
    }

}
