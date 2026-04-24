package me.june8th.ticketrushserver.services;

import me.june8th.ticketrushserver.data.User;
import me.june8th.ticketrushserver.data.UserRepository;
import me.june8th.ticketrushserver.security.JwtTokenProvider;
import me.june8th.ticketrushserver.types.Gender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public User registerUser(String fullName, String email, String password, Date birthDate, String genderString) {
        // Validate input
        if (fullName == null || fullName.trim().length() < 3) {
            throw new IllegalArgumentException("Full name must be at least 3 characters long");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        Gender gender = Gender.fromString(genderString);
        if (gender == null) {
            throw new IllegalArgumentException("Invalid gender value");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user with encrypted password
        User user = User.builder()
                .fullName(fullName.trim())
                .email(email.trim())
                .passwordHash(passwordEncoder.encode(password))
                .birthDate(birthDate)
                .gender(gender)
                .accountNonLocked(true)
                .build();

        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public String generateAccessToken(Long userId, String email) {
        return tokenProvider.generateAccessToken(userId, email);
    }

    public String generateRefreshToken(Long userId, String email) {
        return tokenProvider.generateRefreshToken(userId, email);
    }

}

