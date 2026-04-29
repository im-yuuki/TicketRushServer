package me.june8th.ticketrushserver.services;

import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.data.UserAccountRepository;
import me.june8th.ticketrushserver.security.JwtTokenProvider;
import me.june8th.ticketrushserver.types.Gender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserAccount registerUser(String name, String email, String password, Date birthDate, String genderString) {
        // Validate input
        if (name == null || name.trim().length() < 3) {
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

        // Check if userAccount already exists
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new userAccount with encrypted password
        UserAccount userAccount = UserAccount.builder()
                .name(name.trim())
                .email(email.trim())
                .passwordHash(passwordEncoder.encode(password))
                .birthDate(birthDate)
                .gender(gender)
                .accountNonLocked(true)
                .build();

        return userAccountRepository.save(userAccount);
    }

    public UserAccount loginUser(String email, String password) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return userAccount;
    }

    public String generateAccessToken(Long userId) {
        return tokenProvider.generateAccessToken(userId, 0);
    }

}
