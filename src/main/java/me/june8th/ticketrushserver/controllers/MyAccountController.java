package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.StorageService;
import me.june8th.ticketrushserver.types.OperationResult;
import me.june8th.ticketrushserver.utils.CookieUtils;
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
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<BasicUserInfo> getBasicInfo(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(new BasicUserInfo(storageService, accountService.getAccountData(id)));
    }

    @PutMapping("/name")
    public ResponseEntity<OperationResult> changeName(@AuthenticationPrincipal Long id, @RequestBody UpdateNamePayload request) {
        accountService.changeAccountName(id, request.newName());
        return ResponseEntity.ok(OperationResult.success("Name changed successfully"));
    }

    @PutMapping("/email")
    public ResponseEntity<OperationResult> changeEmail(@AuthenticationPrincipal Long id, @RequestBody UpdateEmailPayload request) {
        accountService.changeAccountEmail(id, request.newEmail(), request.currentPassword());
        return ResponseEntity.ok(OperationResult.success("Email changed successfully"));
    }

    @PutMapping("/password")
    public ResponseEntity<OperationResult> changePassword(@AuthenticationPrincipal Long id, @RequestBody UpdatePasswordPayload request) {
        accountService.changeAccountPassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(OperationResult.success("Password changed successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<OperationResult> logoutAllDevices(Authentication authentication, HttpServletResponse response) {
        accountService.accountLogoutAllSessions((Long) Objects.requireNonNull(authentication.getPrincipal()));
        CookieUtils.deleteHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
        return ResponseEntity.ok(OperationResult.success("Logged out from all devices. Please log in again."));
    }

    public record BasicUserInfo(long id, String name, String email, String avatarUrl, String role) {

        public BasicUserInfo(StorageService storageService, Account account) {
            String avatarUrl = switch (account) {
                case UserAccount userAccount -> storageService.generatePresignedUrl(userAccount.getAvatarKey());
                case OrganizationAccount organizationAccount -> storageService.generatePresignedUrl(organizationAccount.getAvatarKey());
                default -> null;
            };
            this(account.getId(), account.getName(), account.getEmail(), avatarUrl, account.getRole().name());
        }

    }

    public record UpdateNamePayload(String newName) {}

    public record UpdateEmailPayload(String newEmail, String currentPassword) {}

    public record UpdatePasswordPayload(String currentPassword, String newPassword) {}

}
