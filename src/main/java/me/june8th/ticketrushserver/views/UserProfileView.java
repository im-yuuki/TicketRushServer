package me.june8th.ticketrushserver.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
public final class UserProfileView extends ProfileView {

    private String avatarUrl;
    private String birthDate;
    private String country;
    private String gender;
    private String phoneNumber;
    private String addressLine;

}
