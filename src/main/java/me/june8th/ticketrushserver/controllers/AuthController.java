package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.june8th.ticketrushserver.services.AccountService;
import me.june8th.ticketrushserver.services.EmailService;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.CookieUtils;
import me.june8th.ticketrushserver.utils.OperationResponse;
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
    public ResponseEntity<OperationResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        String key = accountService.userRegisterRequest(request.name(), request.email(), request.password(), request.birthDate(), request.gender());
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.OPERATION_ID_COOKIE_NAME, key, operationExpiration);
        return ResponseEntity.status(HttpStatus.CREATED).body(OperationResponse.success("Waiting for confirmation"));
    }

    @GetMapping("/register/confirmation")
    public ResponseEntity<OperationResponse> sendRegistrationOtp(HttpServletRequest request) throws MessagingException {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        emailService.sendRegisterConfirmationEmail(Objects.requireNonNull(key));
        return ResponseEntity.ok(OperationResponse.success("Please check your email for the OTP code to confirm your registration"));
    }

    @PostMapping("/register/confirmation")
    public ResponseEntity<OperationResponse> confirmRegistration(@RequestBody OtpConfirmationRequest requestBody, HttpServletRequest request, HttpServletResponse response) {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        String accessToken = accountService.generateAccessToken(accountService.userRegisterConfirm(Objects.requireNonNull(key), requestBody.otpCode()));
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpiration);
        return ResponseEntity.ok(OperationResponse.success("Registration successful."));
    }

    @PostMapping("/login")
    public ResponseEntity<OperationResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        String accessToken = accountService.generateAccessToken(accountService.accountLogin(request.email(), request.password()));
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpiration);
        return ResponseEntity.ok(OperationResponse.success("Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<OperationResponse> logout(HttpServletResponse response) {
        CookieUtils.deleteHttpOnlyCookie(response, CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
        return ResponseEntity.ok(OperationResponse.success("Logout successful"));
    }

    @PostMapping("/reset")
    public ResponseEntity<OperationResponse> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletResponse response) {
        String key = accountService.accountResetPasswordRequest(request.email(), request.newPassword());
        CookieUtils.setHttpOnlyCookie(response, CookieUtils.OPERATION_ID_COOKIE_NAME, key, operationExpiration);
        return ResponseEntity.status(HttpStatus.CREATED).body(OperationResponse.success("Reset password token created"));
    }

    @GetMapping("/reset/confirm")
    public ResponseEntity<OperationResponse> sendResetPasswordOtp(HttpServletRequest request) throws MessagingException {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        emailService.sendPasswordResetEmail(Objects.requireNonNull(key));
        return ResponseEntity.ok(OperationResponse.success("Please check your email for the OTP code to confirm your password reset"));
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<OperationResponse> confirmResetPassword(@RequestBody OtpConfirmationRequest requestBody, HttpServletRequest request) {
        String key = CookieUtils.getCookie(request, CookieUtils.OPERATION_ID_COOKIE_NAME);
        accountService.accountResetPasswordConfirm(Objects.requireNonNull(key), requestBody.otpCode());
        return ResponseEntity.ok(OperationResponse.success("Password reset successful"));
    }

    public record RegisterRequest(String name, String email, String password, Date birthDate, Gender gender) {}

    public record OtpConfirmationRequest(String otpCode) {}

    public record LoginRequest(String email, String password) {}

    public record ResetPasswordRequest (String email, String newPassword) {}

}
