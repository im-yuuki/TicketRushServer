package me.june8th.ticketrushserver.utils;

import me.june8th.ticketrushserver.types.ValidateError;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    @Test
    void validateName_shouldAcceptTrimmedNameWithinLimits() {
        Validator validator = Validator.create().validateName("  Alice Doe  ");

        assertTrue(validator.isValid());
    }

    @Test
    void validateName_shouldRejectShortName() {
        Validator validator = Validator.create().validateName("Al");

        assertEquals(ValidateError.NAME_TOO_SHORT, validator.getError());
    }

    @Test
    void validateEmail_shouldRejectInvalidEmailFormat() {
        Validator validator = Validator.create().validateEmail("alice-at-example.com");

        assertEquals(ValidateError.EMAIL_INVALID, validator.getError());
    }

    @Test
    void validatePassword_shouldRejectPasswordWithoutDigit() {
        Validator validator = Validator.create().validatePassword("Password");

        assertEquals(ValidateError.PASSWORD_MISSING_CASES, validator.getError());
    }

    @Test
    void validateBirthDate_shouldAcceptAdultBirthDate() {
        long now = System.currentTimeMillis();
        Date validBirthDate = new Date(now - (20L * 365 * 24 * 60 * 60 * 1000));
        Validator validator = Validator.create().validateBirthDate(validBirthDate);

        assertTrue(validator.isValid());
    }

    @Test
    void validateBirthDate_shouldRejectTooYoungBirthDate() {
        long now = System.currentTimeMillis();
        Date invalidBirthDate = new Date(now - (10L * 365 * 24 * 60 * 60 * 1000));
        Validator validator = Validator.create().validateBirthDate(invalidBirthDate);

        assertEquals(ValidateError.BIRTHDATE_INVALID, validator.getError());
    }

    @Test
    void validateOtpCode_shouldRejectNonNumericCode() {
        Validator validator = Validator.create().validateOtpCode("12AB56");

        assertEquals(ValidateError.OTPCODE_INVALID, validator.getError());
    }

    @Test
    void validateRequestKey_shouldRejectWrongLength() {
        Validator validator = Validator.create().validateRequestKey("short-key");

        assertEquals(ValidateError.REQUESTKEY_INVALID, validator.getError());
    }

    @Test
    void validateImageFile_shouldAcceptValidPngHeader() {
        byte[] content = new byte[1024];
        content[0] = (byte) 0x89;
        content[1] = 0x50;
        content[2] = 0x4E;
        content[3] = 0x47;
        content[4] = 0x0D;
        content[5] = 0x0A;
        content[6] = 0x1A;
        content[7] = 0x0A;
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", content);

        Validator validator = Validator.create().validateImageFile(file, 2 * 1024 * 1024);

        assertTrue(validator.isValid());
    }

    @Test
    void validateImageFile_shouldRejectBadHeader() {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", content);

        Validator validator = Validator.create().validateImageFile(file, 2 * 1024 * 1024);

        assertEquals(ValidateError.IMAGE_INVALID, validator.getError());
    }

    @Test
    void throwExceptionIfInvalid_shouldUseValidationMessage() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Validator.create().validatePassword("short").throwExceptionIfInvalid()
        );

        assertEquals(ValidateError.PASSWORD_TOO_SHORT.getMessage(), exception.getMessage());
    }

}
