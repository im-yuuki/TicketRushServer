package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.services.AuthService;
import me.june8th.ticketrushserver.services.EmailService;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.types.OperationResponse;
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

    public AuthController(
            AuthService authService,
            EmailService emailService,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {
        this.authService = authService;
        this.emailService = emailService;
        this.accessTokenExpiration = Math.toIntExact(accessTokenExpiration);
    }

    @PostMapping("/register")
    public ResponseEntity<OperationResponse> register(@RequestBody RegisterRequest request) {
        try {
            String key = authService.userRegisterRequest(request.getName(), request.getEmail(), request.getPassword(), request.getBirthDate(), request.getGender());
            OperationResponse response = OperationResponse.success("Waiting for confirmation");
            response.addMetadataEntry("confirm_key", key);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
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
            UserAccount userAccount = authService.userRegisterConfirm(key, request.getOtpCode());
            return ResponseEntity.ok(OperationResponse.success("Registration successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<OperationResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            UserAccount userAccount = authService.userLogin(request.getEmail(), request.getPassword());

            // TODO: Generate access token and set it as an HTTP-only cookie
            // String accessToken = authService.generateAccessToken(userAccount.getId());
            //
            // Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            // accessTokenCookie.setHttpOnly(true);
            // accessTokenCookie.setSecure(true);
            // accessTokenCookie.setPath("/");
            // accessTokenCookie.setMaxAge(accessTokenExpiration);
            // response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(OperationResponse.success("Login successful"));
        } catch (IllegalArgumentException e) {
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
            authService.userResetPasswordRequest(request.getEmail(), request.getNewPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(OperationResponse.success("Reset password token created"));
        } catch (IllegalArgumentException e) {
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
            authService.userResetPasswordConfirm(key, request.getOtpCode());
            return ResponseEntity.ok(OperationResponse.success("Password reset successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResponse.failure(e.getMessage()));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NonNull
        private String name;

        @NonNull
        private String email;

        @NonNull
        private String password;

        @NonNull
        private Date birthDate;

        @NonNull
        private Gender gender;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NonNull
        private String email;

        @NonNull
        private String password;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {

        @NonNull
        private String email;

        @NonNull
        private String newPassword;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpConfirmationRequest {

        @NonNull
        private String otpCode;

    }

}

