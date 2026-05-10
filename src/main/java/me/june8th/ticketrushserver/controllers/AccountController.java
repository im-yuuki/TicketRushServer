package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.views.OperationResponse;
import me.june8th.ticketrushserver.utils.CookieUtils;
import me.june8th.ticketrushserver.views.ProfileView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ProfileView> getProfile(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(accountService.getAccountProfile(id));
    }

    @PatchMapping
    public ResponseEntity<OperationResponse> updateProfile(@RequestBody ProfileView profile, Authentication authentication) {
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

}
