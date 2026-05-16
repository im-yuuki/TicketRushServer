package me.june8th.ticketrushserver.types;

import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.utils.Validator;

import java.util.Date;
import java.util.Optional;

public record UpdateUserInfoPayload(
        Optional<Date> birthDate,
        Optional<Gender> gender,
        Optional<String> phoneNumber,
        Optional<String> addressLine
) {

    public UserAccount patchUser(UserAccount user) {
        Validator validator = new Validator();
        birthDate.ifPresent(date -> {
            validator.validateBirthDate(date);
            user.setBirthDate(date);
        });
        gender.ifPresent(user::setGender);
        phoneNumber.ifPresent(phoneNumber -> {
            String trimmedPhoneNumber = phoneNumber.trim();
            if (trimmedPhoneNumber.isEmpty()) {
                user.setPhoneNumber(null);
                return;
            }
            validator.validateOptionalPhoneNumber(trimmedPhoneNumber);
            user.setPhoneNumber(trimmedPhoneNumber);
        });
        addressLine.ifPresent(addressLine -> {
            String trimmedAddressLine = addressLine.trim();
            if (trimmedAddressLine.isEmpty()) {
                user.setAddressLine(null);
                return;
            }
            validator.validateNotBlank(trimmedAddressLine);
            user.setAddressLine(trimmedAddressLine);
        });
        validator.throwExceptionIfInvalid();
        return user;
    }

}
