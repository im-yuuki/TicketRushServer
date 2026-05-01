package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.services.AuthService;
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

    private final AuthService authService;
    private final EmailService emailService;
    private final int accessTokenExpiration;

    public AuthController(AuthService authService, EmailService emailService, @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {
        this.authService = authService;
        this.emailService = emailService;
        this.accessTokenExpiration = Math.toIntExact(accessTokenExpiration);
    }

    @PostMapping("/register")
    public ResponseEntity<OperationResponse> register(@RequestBody RegisterRequest request) {
        try {
            String key = authService.userRegisterRequest(request.name(), request.email(), request.password(), request.birthDate(), request.country());
            OperationResponse response = OperationResponse.success("Waiting for confirmation");
            response.addMetadataEntry("confirm_key", key);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @GetMapping("/register/{key}")
    public ResponseEntity<OperationResponse> sendRegistrationOtp(@PathVariable String key) {
        try {
            emailService.sendRegisterConfirmationEmail(key);
            return ResponseEntity.ok(OperationResponse.success("Please check your email for the OTP code to confirm your registration"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/register/{key}")
    public ResponseEntity<OperationResponse> confirmRegistration(@PathVariable String key, @RequestBody OtpConfirmationRequest request) {
        try {
            authService.userRegisterConfirm(key, request.otpCode());
            return ResponseEntity.ok(OperationResponse.success("Registration successful."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<OperationResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Account account = authService.accountLogin(request.email(), request.password());

            String accessToken = authService.generateAccessToken(account);
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(accessTokenExpiration);
            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(OperationResponse.success("Login successful")
                    .addMetadataEntry("account_type", account.getType().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(OperationResponse.failure(e.getMessage()));
        }
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
        try {
            String key = authService.accountResetPasswordRequest(request.email(), request.newPassword());
            OperationResponse response = OperationResponse.success("Reset password token created");
            response.addMetadataEntry("confirm_key", key);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @GetMapping("/reset/{key}")
    public ResponseEntity<OperationResponse> sendResetPasswordOtp(@PathVariable String key) {
        try {
            emailService.sendPasswordResetEmail(key);
            return ResponseEntity.ok(OperationResponse.success("Please check your email for the OTP code to confirm your password reset"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/reset/{key}")
    public ResponseEntity<?> confirmResetPassword(@PathVariable String key, @RequestBody OtpConfirmationRequest request) {
        try {
            authService.accountResetPasswordConfirm(key, request.otpCode());
            return ResponseEntity.ok(OperationResponse.success("Password reset successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    public record RegisterRequest(String name, String email, String password, Date birthDate, Country country) {}

    public record OtpConfirmationRequest(String otpCode) {}

    public record LoginRequest(String email, String password) {}

    public record ResetPasswordRequest (String email, String newPassword) {}

}

