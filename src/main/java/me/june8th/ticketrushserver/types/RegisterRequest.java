package me.june8th.ticketrushserver.types;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class RegisterRequest {

    private final String name;
    private final String email;
    private final String password;
    private final Date birthDate;
    private final Gender gender;
    private final String addressLine;
    private final Country country;

    @Nullable
    private String phoneNumber;

}