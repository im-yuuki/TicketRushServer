package me.june8th.ticketrushserver.services;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.*;
import me.june8th.ticketrushserver.temp.RegisterRequest;
import me.june8th.ticketrushserver.temp.ResetPasswordRequest;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.temp.RegisterRequestRepository;
import me.june8th.ticketrushserver.temp.ResetPasswordRequestRepository;
import me.june8th.ticketrushserver.repositories.UserRepository;
import me.june8th.ticketrushserver.types.AccessTokenData;
import me.june8th.ticketrushserver.security.AccessTokenProvider;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.Validator;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenProvider accessTokenProvider;
    private final RegisterRequestRepository registerRequestRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;

    /**
     * Create a new user registration request. This will create a new entry in the RegisterPayload table.
     *
     * @param name      full name of the user
     * @param email     email address (must be unique)
     * @param password  plain text password (will be hashed before saving)
     * @param birthDate birth date
     * @param gender    gender
     * @return key of the created RegisterPayload, which can be used to confirm the registration
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
            throw new RatelimitedException("A registration session with this email already exists");
        }

        if (accountRepository.existsByEmail(email)) {
            throw new ResourceConflictException("This email is already registered");
        }

        String passwordHash = passwordEncoder.encode(password);
        assert passwordHash != null;

        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .birthDate(birthDate)
                .genderString(gender.toString())
                .build();

        registerRequestRepository.save(registerRequest);
        logger.debug("Successfully created registration request with key: {} for email: {}", registerRequest.getKey(), email);
        return registerRequest.getKey();
    }

    /**
     * Confirm a user registration request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the registration request is still valid, a new UserAccount will be created.
     *
     * @param key     the key of the RegisterPayload entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     */
    @NullMarked
    @Transactional
    public Account userRegisterConfirm(String key, String otpCode) {
        Validator.create()
                .validateRequestKey(key)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        RegisterRequest registerRequest = registerRequestRepository.findByKey(key).orElseThrow(
                () -> {
                    logger.warn("Registration request with key {} not found.", key);
                    return new ResourceNotFoundException("Invalid request");
                }
        );

        Gender gender = Gender.fromString(registerRequest.getGenderString());

        if (Instant.now().isAfter(registerRequest.getExpiresAt())) {
            throw new TimedOutException("This registration request has expired");
        }

        if (registerRequest.getAvailableAttempts() <= 0) {
            throw new RatelimitedException("No available attempts left for this registration request");
        }

        if (!registerRequest.getOtpCode().equals(otpCode)) {
            registerRequest.setAvailableAttempts(registerRequest.getAvailableAttempts() - 1);
            registerRequestRepository.save(registerRequest);
            throw new AuthenticationFailedException("Invalid OTP code");
        }

        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new InvalidStateException("This email is already registered");
        }

        UserAccount userAccount = UserAccount.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .passwordHash(registerRequest.getPasswordHash())
                .birthDate(registerRequest.getBirthDate())
                .gender(gender)
                .build();

        try {
            registerRequestRepository.delete(registerRequest);
        } catch (Exception e) {
            logger.warn("Failed to delete registration request with key {}: {}", key, e.getMessage());
        }
        Account savedAccount = userRepository.save(userAccount);
        logger.debug("Successfully confirmed registration and created user account with ID: {} for email: {}", savedAccount.getId(), savedAccount.getEmail());
        return savedAccount;
    }

    /**
     * Authenticate a user by their email and password.
     * If the credentials are correct, return the corresponding UserAccount.
     *
     * @param email    the user's email address
     * @param password the user's plain text password
     * @return the authenticated UserAccount
     */
    @NullMarked
    public Account accountLogin(String email, String password) {
        Validator.create()
                .validateEmail(email)
                .throwExceptionIfInvalid();

        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new AuthenticationFailedException("Invalid credentials")
        );

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid credentials");
        }

        logger.debug("Account {} ({}) logged in successfully", account.getId(), account.getName());
        return account;
    }

    /**
     * Generate a new access token for the specified account.
     *
     * @param account account
     * @return token string
     */
    @NullMarked
    public String generateAccessToken(Account account) {
        return accessTokenProvider.generateAccessToken(AccessTokenData.builder()
                .id(account.getId())
                .role(account.getRole())
                .domain(account.getDomain())
                .version(account.getTokenVersion())
                .build());
    }

    /**
     * Invalidate all active sessions for the specified account by incrementing the token version.
     *
     * @param accountId the ID of the account whose sessions should be invalidated
     */
    @NullMarked
    @Transactional
    public void accountLogoutAllSessions(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);
        logger.debug("Successfully invalidated all sessions for account ID: {}", accountId);
    }

    /**
     * @param email       the email address of the user requesting a password reset
     * @param newPassword the new plain text password for that user (will be hashed before saving)
     * @return the key of the created ResetPasswordPayload, which can be used to confirm the password reset
     */
    @NullMarked
    @Transactional
    public String accountResetPasswordRequest(String email, String newPassword) {
        Validator.create()
                .validateEmail(email)
                .validatePassword(newPassword)
                .throwExceptionIfInvalid();

        if (resetPasswordRequestRepository.findByEmail(email).isPresent()) {
            throw new RatelimitedException("A password reset session with this account already exists");
        }

        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("This email is not registered")
        );

        String newPasswordHash = passwordEncoder.encode(newPassword);
        assert newPasswordHash != null;

        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .accountId(account.getId())
                .email(email)
                .oldPasswordHash(account.getPasswordHash())
                .newPasswordHash(newPasswordHash)
                .build();

        resetPasswordRequestRepository.save(resetPasswordRequest);
        logger.debug("Successfully created password reset request with key: {} for email: {}", resetPasswordRequest.getKey(), email);
        return resetPasswordRequest.getKey();
    }

    /**
     * Confirm a user password reset request by providing the OTP code sent to the user's email address.
     * If the OTP code is correct and the password reset request is still valid, the user's password will be updated.
     *
     * @param token   the key of the ResetPasswordPayload entry to confirm
     * @param otpCode the OTP code sent to the user's email address
     */
    @NullMarked
    @Transactional
    public void accountResetPasswordConfirm(String token, String otpCode) {
        Validator.create()
                .validateRequestKey(token)
                .validateOtpCode(otpCode)
                .throwExceptionIfInvalid();

        ResetPasswordRequest resetPasswordRequest = resetPasswordRequestRepository.findByKey(token).orElseThrow(
                () -> {
                    logger.warn("Password reset request with token {} not found.", token);
                    return new ResourceNotFoundException("Invalid request");
                }
        );

        if (Instant.now().isAfter(resetPasswordRequest.getExpiresAt())) {
            throw new TimedOutException("This password reset request has expired");
        }

        if (resetPasswordRequest.getAvailableAttempts() <= 0) {
            throw new RatelimitedException("No available attempts left for this password reset request");
        }

        if (!resetPasswordRequest.getOtpCode().equals(otpCode)) {
            resetPasswordRequest.setAvailableAttempts(resetPasswordRequest.getAvailableAttempts() - 1);
            resetPasswordRequestRepository.save(resetPasswordRequest);
            throw new AuthenticationFailedException("Invalid OTP code");
        }

        Account account = accountRepository.findById(resetPasswordRequest.getAccountId()).orElseThrow(
                () -> new InvalidStateException("Invalid request")
        );
        if (!account.getEmail().equals(resetPasswordRequest.getEmail())) {
            throw new InvalidStateException("Invalid request");
        }
        if (!account.getPasswordHash().equals(resetPasswordRequest.getOldPasswordHash())) {
            throw new InvalidStateException("Invalid request");
        }

        account.setPasswordHash(resetPasswordRequest.getNewPasswordHash());
        accountRepository.save(account);

        logger.debug("Password reset successful for account {} ({})", account.getId(), account.getEmail());
        try {
            resetPasswordRequestRepository.delete(resetPasswordRequest);
        } catch (Exception e) {
            logger.warn("Failed to delete password reset request with key {}: {}", token, e.getMessage());
        }
    }

    @NullMarked
    public Account getAccountData(Long id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
    }

    /**
     * Change the name of an account.
     *
     * @param accountId account ID
     * @param newName   new full name
     */
    @NullMarked
    @Transactional
    public void changeAccountName(Long accountId, String newName) {
        Validator.create()
                .validateName(newName)
                .throwExceptionIfInvalid();
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        account.setName(newName);
        accountRepository.save(account);
        logger.debug("Successfully changed name for account ID: {} to {}", accountId, newName);
    }

    /**
     * Change the email of an account.
     *
     * @param accountId       account ID
     * @param newEmail        new email address
     * @param currentPassword current password of the account
     */
    @NullMarked
    @Transactional
    public void changeAccountEmail(Long accountId, String newEmail, String currentPassword) {
        Validator.create()
                .validateEmail(newEmail)
                .throwExceptionIfInvalid();

        if (accountRepository.existsByEmail(newEmail)) {
            throw new ResourceConflictException("This email is already registered");
        }
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            String oldEmail = account.getEmail();
            account.setEmail(newEmail);
            accountRepository.save(account);
            logger.debug("Successfully changed email for account ID: {} from {} to {}", accountId, oldEmail, newEmail);
            return;
        }
        logger.debug("Incorrect password provided for account ID: {} during email change.", accountId);
        throw new AuthenticationFailedException("Incorrect password");
    }

    /**
     * Change the password of an account.
     *
     * @param accountId       account ID
     * @param currentPassword current password
     * @param newPassword     new password
     */
    @NullMarked
    @Transactional
    public void changeAccountPassword(Long accountId, String currentPassword, String newPassword) {
        Validator.create()
                .validatePassword(newPassword)
                .throwExceptionIfInvalid();
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        if (passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            account.setPasswordHash(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            logger.debug("Successfully changed password for account ID: {}", accountId);
            return;
        }
        logger.debug("Incorrect password provided for account ID: {} during password change.", accountId);
        throw new AuthenticationFailedException("Incorrect password");
    }

}
