package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.ManagerAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.repositories.ManagerAccountRepository;
import me.june8th.ticketrushserver.repositories.UserAccountRepository;
import me.june8th.ticketrushserver.security.JwtTokenProvider;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;
    private final ManagerAccountRepository managerAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public UserAccount userRegister(String name, String email, String password, Date birthDate, String genderString) {

        Validator.create()
                .validateName(name)
                .validateEmail(email)
                .validatePassword(password)
                .throwIfInvalid();

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

    public UserAccount userLogin(String email, String password) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return userAccount;
    }

    public boolean userLogoutAllSessions(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userAccount.setTokenVersion(userAccount.getTokenVersion() + 1);
        userAccountRepository.save(userAccount);
        return true;
    }

    public boolean userCreateResetPasswordToken(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // TODO: send email with reset password token
        return true;
    }

    public boolean userResetPassword(String token, String newPassword) {
        // TODO: implement reset password logic
        return false;
    }


    public ManagerAccount managerLogin(String email, String password) {
        ManagerAccount managerAccount = managerAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, managerAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return managerAccount;
    }

    public String generateAccessToken(Long userId) {
        return tokenProvider.generateAccessToken(userId, 0);
    }

}
