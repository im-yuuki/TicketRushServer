package me.june8th.ticketrushserver.controllers;

import lombok.Builder;
import lombok.Data;
import me.june8th.ticketrushserver.types.AccountType;
import me.june8th.ticketrushserver.types.NotImplementedException;
import me.june8th.ticketrushserver.types.OperationResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    @GetMapping
    public String getProfile() {
        throw new NotImplementedException();
    }

    @PatchMapping
    public String updateProfile() {
        throw new NotImplementedException();
    }

    @PatchMapping("/avatar")
    public String changeAvatar() {
        throw new NotImplementedException();
    }

    @PatchMapping("/email")
    public OperationResponse changeEmail(@RequestBody UpdateEmailRequest request) {
        throw new NotImplementedException();
    }

    @PatchMapping("/password")
    public OperationResponse changePassword(@RequestBody UpdatePasswordRequest request) {
        throw new NotImplementedException();
    }

    @PostMapping("/revoke-all-sessions")
    public OperationResponse revokeAllSessions() {
        throw new NotImplementedException();
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
