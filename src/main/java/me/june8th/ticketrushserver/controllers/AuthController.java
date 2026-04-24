package me.june8th.ticketrushserver.controllers;

import lombok.*;
import me.june8th.ticketrushserver.data.User;
import me.june8th.ticketrushserver.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.loginUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), "Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        }
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

