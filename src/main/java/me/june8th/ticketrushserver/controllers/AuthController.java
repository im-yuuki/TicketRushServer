package me.june8th.ticketrushserver.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import me.june8th.ticketrushserver.data.User;
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
    private final int refreshTokenExpiration;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.authService = authService;
        // convert milliseconds to seconds for cookie maxAge
        this.accessTokenExpiration = Math.toIntExact(accessTokenExpiration / 1000);
        this.refreshTokenExpiration = Math.toIntExact(refreshTokenExpiration / 1000);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(request.getFullName(), request.getEmail(), request.getPassword(), request.getBirthDate(), request.getGender());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(user.getId(), user.getEmail(), "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            User user = authService.loginUser(request.getEmail(), request.getPassword());

            // Generate tokens
            String accessToken = authService.generateAccessToken(user.getId(), user.getEmail());
            String refreshToken = authService.generateRefreshToken(user.getId(), user.getEmail());

            // Set access token in httpOnly cookie
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(accessTokenExpiration);
            response.addCookie(accessTokenCookie);

            // Set refresh token in httpOnly cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(refreshTokenExpiration);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), "Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear access token cookie
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        // Clear refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new ErrorResponse("Logout successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletResponse response) {
        // This endpoint should be called with valid refreshToken cookie
        // The JWT filter will have already validated it
        // In a real scenario, you would extract user info and generate new access token
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ErrorResponse("Refresh token endpoint needs to be implemented with session management"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NonNull
        private String fullName;
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

