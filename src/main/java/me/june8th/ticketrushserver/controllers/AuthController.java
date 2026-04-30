package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.services.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final int accessTokenExpiration;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {
        this.authService = authService;
        this.accessTokenExpiration = Math.toIntExact(accessTokenExpiration);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserAccount userAccount = authService.userRegister(request.getName(), request.getEmail(), request.getPassword(), request.getBirthDate(), request.getGender());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(userAccount.getId(), userAccount.getEmail(), "UserAccount registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            UserAccount userAccount = authService.userLogin(request.getEmail(), request.getPassword());
            String accessToken = authService.generateAccessToken(userAccount.getId());

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(accessTokenExpiration);
            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(new AuthResponse(userAccount.getId(), userAccount.getEmail(), "Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(new ErrorResponse("Logout successful"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return null;
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
        private String gender;

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

    }

    @Data
    public static class AuthResponse {

        private final boolean success = true;

        @NonNull
        private Long id;

        @NonNull
        private String email;

        @NonNull
        private String message;

    }

    @Data
    public static class ErrorResponse {

        private final boolean success = false;

        @NonNull
        private String error;

    }

}

