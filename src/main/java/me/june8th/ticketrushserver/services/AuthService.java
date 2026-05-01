package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.ManagerAccount;
import me.june8th.ticketrushserver.data.RegisterRequest;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.data.ResetPasswordRequest;
import me.june8th.ticketrushserver.repositories.ManagerAccountRepository;
import me.june8th.ticketrushserver.repositories.RegisterRequestRepository;
import me.june8th.ticketrushserver.repositories.ResetPasswordRequestRepository;
import me.june8th.ticketrushserver.repositories.UserAccountRepository;
import me.june8th.ticketrushserver.security.JwtTokenProvider;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;
    private final ManagerAccountRepository managerAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RegisterRequestRepository registerRequestRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;
    private final EmailService emailService;

    /**
     * Create a new user registration request. This will create a new entry in the RegisterRequest table.
     *
     * @param name full name of the user
     * @param email email address (must be unique)
     * @param password plain text password (will be hashed before saving)
     * @param birthDate birth date
     * @param gender gender
     * @return key of the created RegisterRequest, which can be used to confirm the registration
     */
    @NullMarked
    @Transactional
    public String userRegisterRequest(String name, String email, String password, Date birthDate, Gender gender) {
        Validator.create()
                .validateName(name)
                .validateEmail(email)
                .validatePassword(password)
                .validateBirthDate(birthDate)
                .throwExceptionIfInvalid();

        if (registerRequestRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A registration session with this email already exists");
        }

        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("This email is already registered");
        }

        String passwordHash = passwordEncoder.encode(password);
        assert passwordHash != null;

        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .birthDate(birthDate)
                .gender(gender)
                .build();

        logger.debug("Creating registration request {} for email {}", registerRequest.getKey(), email);
        registerRequestRepository.save(registerRequest);
        return registerRequest.getKey();
    }

    /**
     * Confirm a user registration request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the registration request is still valid, a new UserAccount will be created.
     *
     * @param key the key of the RegisterRequest entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     * @return created UserAccount if the registration is successful
     */
    @NullMarked
    @Transactional
    public UserAccount userRegisterConfirm(String key, String otpCode) {
        Validator.create()
                .validateRequestKey(key)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        RegisterRequest registerRequest = registerRequestRepository.findByKey(key).orElseThrow(
                () -> new IllegalArgumentException("Invalid registration key")
        );

        if (Instant.now().isAfter(registerRequest.getExpiresAt())) {
            throw new RuntimeException("This registration request has expired");
        }

        if (registerRequest.getAvailableAttempts() <= 0) {
            throw new RuntimeException("No available attempts left for this registration request");
        }

        if (!registerRequest.getOtpCode().equals(otpCode)) {
            registerRequest.setAvailableAttempts(registerRequest.getAvailableAttempts() - 1);
            registerRequestRepository.save(registerRequest);
            throw new IllegalArgumentException("Invalid OTP code");
        }

        UserAccount userAccount = UserAccount.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .passwordHash(registerRequest.getPasswordHash())
                .birthDate(registerRequest.getBirthDate())
                .gender(registerRequest.getGender())
                .build();

        logger.debug("Validation passed, creating user account for {} ({})", registerRequest.getName(), registerRequest.getEmail());
        try {
            registerRequestRepository.delete(registerRequest);
        } catch (Exception e) {
            logger.warn("Failed to delete registration request with key {}: {}", key, e.getMessage());
        }
        return userAccountRepository.save(userAccount);
    }

    /**
     * Authenticate a user by their email and password.
     * If the credentials are correct, return the corresponding UserAccount.
     *
     * @param email the user's email address
     * @param password the user's plain text password
     * @return the authenticated UserAccount
     */
    @NullMarked
    public UserAccount userLogin(String email, String password) {
        Validator.create()
                .validateEmail(email)
                .validatePassword(password)
                .throwExceptionIfInvalid();

        UserAccount userAccount = userAccountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Invalid email or password")
        );

        if (!passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        logger.debug("User {} ({}) logged in successfully", userAccount.getId(), userAccount.getName());
        return userAccount;
    }

    /**
     * Invalidate all active sessions for the specified user by incrementing the token version.
     *
     * @param userId the ID of the user whose sessions should be invalidated
     * @return true if the operation is successful, false otherwise
     */
    @NullMarked
    @Transactional
    public boolean userLogoutAllSessions(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        userAccount.setTokenVersion(userAccount.getTokenVersion() + 1);
        userAccountRepository.save(userAccount);
        return true;
    }

    /**
     * @param email the email address of the user requesting a password reset
     * @param newPassword the new plain text password for that user (will be hashed before saving)
     * @return the key of the created ResetPasswordRequest, which can be used to confirm the password reset
     */
    @NullMarked
    @Transactional
    public String userResetPasswordRequest(String email, String newPassword) {
        Validator.create()
                .validateEmail(email)
                .validatePassword(newPassword)
                .throwExceptionIfInvalid();

        if (resetPasswordRequestRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A password reset session with this account already exists");
        }

        UserAccount userAccount = userAccountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("This email is not registered")
        );

        String newPasswordHash = passwordEncoder.encode(newPassword);
        assert newPasswordHash != null;

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .userId(userAccount.getId())
                .email(email)
                .newPasswordHash(newPasswordHash)
                .build();

        logger.debug("Creating password reset request {} for email {}", resetPasswordRequest.getKey(), email);
        resetPasswordRequestRepository.save(resetPasswordRequest);
        return resetPasswordRequest.getKey();
    }

    /**
     * Confirm a user password reset request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the password reset request is still valid, the user's password will be updated.
     *
     * @param token the key of the ResetPasswordRequest entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     * @return true if the password reset is successful, false otherwise
     */
    @NullMarked
    @Transactional
    public boolean userResetPasswordConfirm(String token, String otpCode) {
        Validator.create()
                .validateRequestKey(token)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        ResetPasswordRequest resetPasswordRequest = resetPasswordRequestRepository.findByKey(token).orElseThrow(
                () -> new IllegalArgumentException("Invalid password reset key")
        );

        if (Instant.now().isAfter(resetPasswordRequest.getExpiresAt())) {
            throw new RuntimeException("This password reset request has expired");
        }

        if (resetPasswordRequest.getAvailableAttempts() <= 0) {
            throw new RuntimeException("No available attempts left for this password reset request");
        }

        if (!resetPasswordRequest.getOtpCode().equals(otpCode)) {
            resetPasswordRequest.setAvailableAttempts(resetPasswordRequest.getAvailableAttempts() - 1);
            resetPasswordRequestRepository.save(resetPasswordRequest);
            throw new IllegalArgumentException("Invalid OTP code");
        }

        UserAccount userAccount = userAccountRepository.findById(resetPasswordRequest.getUserId()).orElseThrow(
                () -> new IllegalArgumentException("Invalid request")
        );
        if (!userAccount.getEmail().equals(resetPasswordRequest.getEmail())) {
            throw new IllegalArgumentException("Invalid request");
        }

        userAccount.setPasswordHash(resetPasswordRequest.getNewPasswordHash());
        userAccountRepository.save(userAccount);

        logger.debug("Password reset successful for user {} ({})", userAccount.getId(), userAccount.getEmail());
        try {
            resetPasswordRequestRepository.delete(resetPasswordRequest);
        } catch (Exception e) {
            logger.warn("Failed to delete password reset request with key {}: {}", token, e.getMessage());
        }
        return true;
    }

    /**
     * Authenticate a manager by their email and password.
     * If the credentials are correct, return the corresponding ManagerAccount.
     *
     * @param email email address of the manager
     * @param password plain text password
     * @return the authenticated ManagerAccount if the credentials are correct
     */
    public ManagerAccount managerLogin(String email, String password) {
        Validator.create()
                .validateEmail(email)
                .validatePassword(password)
                .throwExceptionIfInvalid();

        ManagerAccount managerAccount = managerAccountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Invalid email or password")
        );
        if (!passwordEncoder.matches(password, managerAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        logger.debug("Manager {} ({}) logged in successfully", managerAccount.getId(), managerAccount.getName());
        return managerAccount;
    }

}
