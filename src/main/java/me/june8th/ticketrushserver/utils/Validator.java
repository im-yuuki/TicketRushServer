package me.june8th.ticketrushserver.utils;

import lombok.Getter;
import me.june8th.ticketrushserver.types.ValidateError;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

public class Validator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PHONE_REGEX = "^\\+?[0-9]{7,15}$";
    private static final long ONE_YEAR_IN_MILLIS = 365L * 24 * 60 * 60 * 1000;
    private static final Collection<String> allowedImageTypes = Set.of("image/jpeg", "image/png", "image/webp");

    @Getter
    private ValidateError error = ValidateError.NONE;

    private boolean internalCommonBreakMethod(Object object) {
        if (error != ValidateError.NONE) return true;
        if (object == null) {
            error = ValidateError.MISSING_REQUIRED_FIELD;
            return true;
        }
        return false;
    }

    public Validator validateNotBlank(String value) {
        if (internalCommonBreakMethod(value)) return this;
        if (value.trim().isEmpty()) error = ValidateError.MISSING_REQUIRED_FIELD;
        return this;
    }

    public Validator validateName(String name) {
        if (internalCommonBreakMethod(name)) return this;
        String trimmedName = name.trim();
        if (trimmedName.length() < 3) error = ValidateError.NAME_TOO_SHORT;
        else if (trimmedName.length() > 50) error = ValidateError.NAME_TOO_LONG;
        return this;
    }

    public Validator validateEmail(String email) {
        if (internalCommonBreakMethod(email)) return this;
        String trimmedEmail = email.trim();
        if (trimmedEmail.length() > 100) {
            error = ValidateError.EMAIL_INVALID;
            return this;
        }
        if (!trimmedEmail.matches(EMAIL_REGEX)) error = ValidateError.EMAIL_INVALID;
        return this;
    }

    public Validator validatePassword(String password) {
        if (internalCommonBreakMethod(password)) return this;
        if (password.length() < 8) {
            error = ValidateError.PASSWORD_TOO_SHORT;
            return this;
        }
        boolean noUpperCase = true;
        boolean noLowerCase = true;
        boolean noDigit = true;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                noUpperCase = false;
            } else if (Character.isLowerCase(c)) {
                noLowerCase = false;
            } else if (Character.isDigit(c)) {
                noDigit = false;
            }
        }
        if (noUpperCase || noLowerCase || noDigit) error = ValidateError.PASSWORD_MISSING_CASES;
        return this;
    }

    public Validator validateBirthDate(Date birthDate) {
        if (internalCommonBreakMethod(birthDate)) return this;
        // not after now - 15 years and not before now - 100 years :)
        long now = System.currentTimeMillis();
        Date minDate = new Date(now - ONE_YEAR_IN_MILLIS * 100);
        Date maxDate = new Date(now - ONE_YEAR_IN_MILLIS * 15);
        if (birthDate.before(minDate) || birthDate.after(maxDate)) error = ValidateError.BIRTHDATE_INVALID;
        return this;
    }

    public Validator validatePastDate(Date date) {
        if (internalCommonBreakMethod(date)) return this;
        if (date.after(new Date())) error = ValidateError.DATE_NOT_IN_PAST;
        return this;
    }

    public Validator validateFutureDate(Date date) {
        if (internalCommonBreakMethod(date)) return this;
        if (date.before(new Date())) error = ValidateError.DATE_NOT_IN_FUTURE;
        return this;
    }

    public Validator validateRequestKey(String key) {
        if (internalCommonBreakMethod(key)) return this;
        String trimmedKey = key.trim();
        if (trimmedKey.length() != RandomGenerator.REQUEST_KEY_LENGTH) error = ValidateError.REQUESTKEY_INVALID;
        return this;
    }

    public Validator validateOtpCode(String otpCode) {
        if (internalCommonBreakMethod(otpCode)) return this;
        String trimmedOtpCode = otpCode.trim();
        if (trimmedOtpCode.length() != RandomGenerator.OTP_CODE_LENGTH || !trimmedOtpCode.matches("\\d+")) {
            error = ValidateError.OTPCODE_INVALID;
        }
        return this;
    }

    public Validator validateOptionalPhoneNumber(String phoneNumber) {
        if (internalCommonBreakMethod(phoneNumber)) return this;
        if (phoneNumber.trim().isEmpty()) return this;
        String trimmedPhone = phoneNumber.trim();
        if (!trimmedPhone.matches(PHONE_REGEX)) error = ValidateError.PHONENUMBER_INVALID;
        return this;
    }

    public Validator validateNaturalNumber(Integer number) {
        if (internalCommonBreakMethod(number)) return this;
        if (number < 0) error = ValidateError.NUMBER_NEGATIVE;
        return this;
    }

    public Validator validateNaturalNumber(Long number) {
        if (internalCommonBreakMethod(number)) return this;
        if (number < 0L) error = ValidateError.NUMBER_NEGATIVE;
        return this;
    }

    public Validator validateUri(String uri) {
        if (internalCommonBreakMethod(uri)) return this;
        try {
            new URI(uri);
        } catch (Exception e) {
            error = ValidateError.URI_INVALID;
        }
        return this;
    }

    public Validator validateImageFile(MultipartFile file, long maxSizeBytes) {
        if (internalCommonBreakMethod(file)) return this;
        try {
            if (file.getSize() > maxSizeBytes || file.getSize() < 1024) {
                throw new FileUploadException("Invalid file size: " + file.getSize() + " bytes");
            }
            String contentType = file.getContentType();
            if (contentType == null || !allowedImageTypes.contains(contentType)) {
                throw new FileUploadException("Unsupported file type: " + contentType);
            }
            // Magic-bytes check (don't trust Content-Type header alone)
            InputStream is = file.getInputStream();
            byte[] header = is.readNBytes(12);
            if (!isValidImageHeader(header)) {
                throw new FileUploadException("File content does not match a valid image");
            }
        } catch (IOException e) {
            error = ValidateError.IMAGE_INVALID;
        }
        return this;
    }

    private boolean isValidImageHeader(byte[] h) {
        // JPEG: FF D8 FF
        if (h.length >= 3 && h[0] == (byte)0xFF && h[1] == (byte)0xD8 && h[2] == (byte)0xFF) return true;
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (h.length >= 8 &&
                h[0]==(byte)0x89 && h[1]==0x50 && h[2]==0x4E && h[3]==0x47 &&
                h[4]==0x0D && h[5]==0x0A && h[6]==(byte)0x1A && h[7]==0x0A) return true;
        // WebP: 52 49 46 46 ?? ?? ?? ?? 57 45 42 50
        if (h.length >= 12 &&
                h[0]==0x52 && h[1]==0x49 && h[2]==0x46 && h[3]==0x46 &&
                h[8]==0x57 && h[9]==0x45 && h[10]==0x42 && h[11]==0x50) return true;
        return false;
    }

    public static Validator create() {
        return new Validator();
    }

    public boolean isValid() {
        return error == ValidateError.NONE;
    }

    public void throwExceptionIfInvalid() throws IllegalArgumentException {
        if (error != ValidateError.NONE) {
            throw new IllegalArgumentException(error.getMessage());
        }
    }

}
