package me.june8th.ticketrushserver.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.views.OperationResult;
import me.june8th.ticketrushserver.utils.CookieUtils;
import me.june8th.ticketrushserver.views.Patchable;
import me.june8th.ticketrushserver.views.Private;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class MyAccountController {

    private final AccountService accountService;

    @GetMapping
    @JsonView(Private.class)
    public ResponseEntity<Account> getProfile(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(accountService.getAccountProfile(id));
    }

    @PatchMapping
    public ResponseEntity<OperationResult> updateProfile(@AuthenticationPrincipal Long id, @RequestBody @JsonView(Patchable.class) Account account) {
        throw new NotImplementedException();
    }

    @PatchMapping("/avatar")
    public ResponseEntity<OperationResult> changeAvatar(@AuthenticationPrincipal Long id) {
        throw new NotImplementedException();
    }

    @PatchMapping("/email")
    public ResponseEntity<OperationResult> changeEmail(@AuthenticationPrincipal Long id, @RequestBody UpdateEmailRequest request) {
        accountService.changeAccountEmail(id, request.newEmail(), request.currentPassword());
        return ResponseEntity.ok(OperationResult.success("Email changed successfully"));
    }

    @PatchMapping("/password")
    public ResponseEntity<OperationResult> changePassword(@AuthenticationPrincipal Long id, @RequestBody UpdatePasswordRequest request) {
        accountService.changeAccountPassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(OperationResult.success("Password changed successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<OperationResult> logoutAllDevices(Authentication authentication, HttpServletResponse response) {
        accountService.accountLogoutAllSessions((Long) Objects.requireNonNull(authentication.getPrincipal()));
        CookieUtils.deleteHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
        return ResponseEntity.ok(OperationResult.success("Logged out from all devices. Please log in again."));
    }

    @Builder
    public record UpdateEmailRequest(String newEmail, String currentPassword) {}

    @Builder
    public record UpdatePasswordRequest(String currentPassword, String newPassword) {}

}
