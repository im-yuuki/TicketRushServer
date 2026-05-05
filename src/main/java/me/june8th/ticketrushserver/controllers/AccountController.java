package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.security.AccessTokenData;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.types.AccountType;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResponse;
import me.june8th.ticketrushserver.utils.CookieUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<ProfileModel> getProfile(Authentication authentication) {
        AccessTokenData authenticationDetails = (AccessTokenData) authentication.getDetails();
        assert authenticationDetails != null;
        Account account = accountRepository.findById(authenticationDetails.id()).orElseThrow(
                () -> new RuntimeException("Account not found")
        );
        if (account.getType() == AccountType.USER) {
            UserAccount userAccount = (UserAccount) account;
            return ResponseEntity.ok(UserProfileModel.builder()
                    .name(userAccount.getName())
                    .email(userAccount.getEmail())
                    .type(userAccount.getType())
                    .avatarUrl(userAccount.getAvatarKey())
                    .birthDate(userAccount.getBirthDate().toString())
                    .country(userAccount.getCountry().toString())
                    .gender(userAccount.getGender().toString())
                    .phoneNumber(userAccount.getPhoneNumber())
                    .addressLine(userAccount.getAddressLine())
                    .build()
            );
        } else if (account.getType() == AccountType.ORGANIZATION) {
            OrganizationAccount organizationAccount = (OrganizationAccount) account;
            return ResponseEntity.ok(OrganizationProfileModel.builder()
                    .name(organizationAccount.getName())
                    .email(organizationAccount.getEmail())
                    .type(organizationAccount.getType())
                    .avatarUrl(organizationAccount.getAvatarKey())
                    .bannerUrl(organizationAccount.getBannerKey())
                    .aliasName(organizationAccount.getAliasName())
                    .description(organizationAccount.getDescription())
                    .websiteUrl(organizationAccount.getWebsiteUrl())
                    .build()
            );
        } else {
            return ResponseEntity.ok(ProfileModel.builder()
                    .name(account.getName())
                    .email(account.getEmail())
                    .type(account.getType())
                    .build()
            );
        }
    }

    @PatchMapping
    public ResponseEntity<OperationResponse> updateProfile() {
        throw new NotImplementedException();
    }

    @PatchMapping("/avatar")
    public ResponseEntity<OperationResponse> changeAvatar() {
        throw new NotImplementedException();
    }

    @PatchMapping("/email")
    public ResponseEntity<OperationResponse> changeEmail(@RequestBody UpdateEmailRequest request) {
        throw new NotImplementedException();
    }

    @PatchMapping("/password")
    public ResponseEntity<OperationResponse> changePassword(@RequestBody UpdatePasswordRequest request) {
        throw new NotImplementedException();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<OperationResponse> logoutAllDevices(Authentication authentication, HttpServletResponse response) {
        accountService.accountLogoutAllSessions((Long) Objects.requireNonNull(authentication.getPrincipal()));
        CookieUtils.deleteHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
        return ResponseEntity.ok(OperationResponse.success("Logged out from all devices. Please log in again."));
    }

    @Builder
    public record UpdateEmailRequest(String newEmail, String currentPassword) {}

    @Builder
    public record UpdatePasswordRequest(String currentPassword, String newPassword) {}

    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class ProfileModel {
        private String name;
        private String email;
        private AccountType type;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @SuperBuilder
    public static class UserProfileModel extends ProfileModel {
        private String avatarUrl;
        private String birthDate;
        private String country;
        private String gender;
        private String phoneNumber;
        private String addressLine;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @SuperBuilder
    public static class OrganizationProfileModel extends ProfileModel {
        private String avatarUrl;
        private String bannerUrl;
        private String aliasName;
        private String description;
        private String websiteUrl;
    }

}
