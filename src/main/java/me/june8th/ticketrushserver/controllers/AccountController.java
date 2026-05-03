package me.june8th.ticketrushserver.controllers;

import lombok.Builder;
import lombok.Data;
import me.june8th.ticketrushserver.types.AccountType;
import me.june8th.ticketrushserver.types.OperationResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    @GetMapping
    public String getProfile() {
        return null;
    }

    @PatchMapping
    public String updateProfile() {
        return null;
    }

    @PatchMapping("/avatar")
    public String changeAvatar() {
        return null;
    }

    @PatchMapping("/email")
    public OperationResponse changeEmail(@RequestBody UpdateEmailRequest request) {
        return null;
    }

    @PatchMapping("/password")
    public OperationResponse changePassword(@RequestBody UpdatePasswordRequest request) {
        return null;
    }

    @PostMapping("/revoke-all-sessions")
    public OperationResponse revokeAllSessions() {
        return null;
    }

    @Builder
    public record UpdateEmailRequest(String newEmail, String currentPassword) {}

    @Builder
    public record UpdatePasswordRequest(String currentPassword, String newPassword) {}

    @Data
    public static class ProfileModel {
        private String name;
        private String email;
        private AccountType type;
    }

}
