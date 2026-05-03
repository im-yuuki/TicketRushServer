package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EmailService;
import me.june8th.ticketrushserver.types.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;
    private final EmailService emailService;
    private final int accessTokenExpiration;

    public AuthController(AccountService accountService, EmailService emailService, @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {
        this.accountService = accountService;
        this.emailService = emailService;
        this.accessTokenExpiration = Math.toIntExact(accessTokenExpiration);
    }

    @PostMapping("/register")
    public ResponseEntity<OperationResponse> register(@RequestBody RegisterRequest request) {
        String key = accountService.userRegisterRequest(request.name(), request.email(), request.password(), request.birthDate(), request.country());
        OperationResponse response = OperationResponse.success("Waiting for confirmation");
        response.addMetadataEntry("confirm_key", key);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/register/{key}")
    public ResponseEntity<OperationResponse> sendRegistrationOtp(@PathVariable String key) throws MessagingException {
        emailService.sendRegisterConfirmationEmail(key);
        return ResponseEntity.ok(OperationResponse.success("Please check your email for the OTP code to confirm your registration"));
    }

    @PostMapping("/register/{key}")
    public ResponseEntity<OperationResponse> confirmRegistration(@PathVariable String key, @RequestBody OtpConfirmationRequest request) {
        accountService.userRegisterConfirm(key, request.otpCode());
        return ResponseEntity.ok(OperationResponse.success("Registration successful."));
    }

    @PostMapping("/login")
    public ResponseEntity<OperationResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Account account = accountService.accountLogin(request.email(), request.password());

        String accessToken = accountService.generateAccessToken(account);
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(accessTokenExpiration);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(OperationResponse.success("Login successful")
                .addMetadataEntry("account_type", account.getType().name()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<OperationResponse> logout(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(OperationResponse.success("Logout successful"));
    }

    @PostMapping("/reset")
    public ResponseEntity<OperationResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        String key = accountService.accountResetPasswordRequest(request.email(), request.newPassword());
        OperationResponse response = OperationResponse.success("Reset password token created");
        response.addMetadataEntry("confirm_key", key);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reset/{key}")
    public ResponseEntity<OperationResponse> sendResetPasswordOtp(@PathVariable String key) throws MessagingException {
        emailService.sendPasswordResetEmail(key);
        return ResponseEntity.ok(OperationResponse.success("Please check your email for the OTP code to confirm your password reset"));
    }

    @PostMapping("/reset/{key}")
    public ResponseEntity<OperationResponse> confirmResetPassword(@PathVariable String key, @RequestBody OtpConfirmationRequest request) {
        accountService.accountResetPasswordConfirm(key, request.otpCode());
        return ResponseEntity.ok(OperationResponse.success("Password reset successful"));
    }

    public record RegisterRequest(String name, String email, String password, Date birthDate, Country country) {}

    public record OtpConfirmationRequest(String otpCode) {}

    public record LoginRequest(String email, String password) {}

    public record ResetPasswordRequest (String email, String newPassword) {}

}
