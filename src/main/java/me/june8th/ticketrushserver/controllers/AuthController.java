package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EmailService;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.CookieUtils;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;
    private final EmailService emailService;
    private final int accessTokenExpiration;
    private final int operationExpiration = 600; // 10 minutes

    public AuthController(AccountService accountService, EmailService emailService, @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {
        this.accountService = accountService;
        this.emailService = emailService;
        this.accessTokenExpiration = Math.toIntExact(accessTokenExpiration);
    }

    @PostMapping("/register")
    public ResponseEntity<OperationResult> register(@RequestBody RegisterPayload payload, HttpServletResponse response) {
        String key = accountService.userRegisterRequest(payload.name(), payload.email(), payload.password(), payload.birthDate(), payload.gender());
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.OPERATION_ID_COOKIE_NAME, key, operationExpiration);
        return ResponseEntity.status(HttpStatus.CREATED).body(OperationResult.success("Waiting for confirmation"));
    }

    @GetMapping("/register/confirmation")
    public ResponseEntity<OperationResult> sendRegistrationOtp(HttpServletRequest payload) throws MessagingException {
        String key = CookieUtils.getCookie(payload, CookieUtils.OPERATION_ID_COOKIE_NAME);
        emailService.sendRegisterConfirmationEmail(Objects.requireNonNull(key));
        return ResponseEntity.ok(OperationResult.success("Please check your email for the OTP code to confirm your registration"));
    }

    @PostMapping("/register/confirmation")
    public ResponseEntity<OperationResult> confirmRegistration(@RequestBody OtpConfirmationPayload payload, HttpServletRequest request, HttpServletResponse response) {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        Account account = accountService.userRegisterConfirm(Objects.requireNonNull(key), payload.otpCode());
        String accessToken = accountService.generateAccessToken(account);
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpiration);
        return ResponseEntity.ok(OperationResult.success("Registration successful", account.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<OperationResult> login(@RequestBody LoginPayload payload, HttpServletResponse response) {
        Account account = accountService.accountLogin(payload.email(), payload.password());
        String accessToken = accountService.generateAccessToken(account);
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpiration);
        return ResponseEntity.ok(OperationResult.success("Login successful", account.getId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<OperationResult> logout(HttpServletResponse response) {
        CookieUtils.deleteHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
        return ResponseEntity.ok(OperationResult.success("Logout successful"));
    }

    @PostMapping("/reset")
    public ResponseEntity<OperationResult> resetPassword(@RequestBody ResetPasswordPayload payload, HttpServletResponse response) {
        String key = accountService.accountResetPasswordRequest(payload.email(), payload.newPassword());
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.OPERATION_ID_COOKIE_NAME, key, operationExpiration);
        return ResponseEntity.status(HttpStatus.CREATED).body(OperationResult.success("Reset password token created"));
    }

    @GetMapping("/reset/confirm")
    public ResponseEntity<OperationResult> sendResetPasswordOtp(HttpServletRequest request) throws MessagingException {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        emailService.sendPasswordResetEmail(Objects.requireNonNull(key));
        return ResponseEntity.ok(OperationResult.success("Please check your email for the OTP code to confirm your password reset"));
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<OperationResult> confirmResetPassword(@RequestBody OtpConfirmationPayload payload, HttpServletRequest request) {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        accountService.accountResetPasswordConfirm(Objects.requireNonNull(key), payload.otpCode());
        return ResponseEntity.ok(OperationResult.success("Password reset successful"));
    }

    public record RegisterPayload(String name, String email, String password, Date birthDate, Gender gender) {}

    public record OtpConfirmationPayload(String otpCode) {}

    public record LoginPayload(String email, String password) {}

    public record ResetPasswordPayload(String email, String newPassword) {}

}
