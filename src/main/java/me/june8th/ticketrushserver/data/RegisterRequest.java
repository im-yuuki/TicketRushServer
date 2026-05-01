package me.june8th.ticketrushserver.data;

import lombok.*;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.utils.RandomGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.Date;

@RedisHash(value = "register_account_request")
@Data
@Builder
public class RegisterRequest {

    @TimeToLive
    @Builder.Default
    private Long ttl = 600L;

    @Id
    @Builder.Default
    private String key = RandomGenerator.generateRequestKey();

    @Builder.Default
    private String otpCode = RandomGenerator.generateOtpCode();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant expiresAt = Instant.now().plusSeconds(600);

    @NonNull
    @Builder.Default
    private Instant nextResendAvailable = Instant.now();

    @NonNull
    @Builder.Default
    private Integer availableAttempts = 5;

    @NonNull
    private String name;

    @Indexed
    @NonNull
    private String email;

    @NonNull
    private String passwordHash;

    @NonNull
    private Date birthDate;

    @NonNull
    private Gender gender;

}
