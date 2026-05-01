package me.june8th.ticketrushserver.data;

import lombok.*;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.utils.RandomGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;
import java.util.Date;

@RedisHash(value = "register_account_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @TimeToLive
    private final Long ttl = 600L;

    @Id
    private final String key = RandomGenerator.generateRequestKey();

    private final String otpCode = RandomGenerator.generateOtpCode();

    private final Instant createdAt = Instant.now();

    private final Instant expiresAt = createdAt.plusSeconds(600);

    @NonNull
    @Builder.Default
    private Instant nextResendAvailable = Instant.now();

    @NonNull
    @Builder.Default
    private Integer availableAttempts = 5;

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private String passwordHash;

    @NonNull
    private Date birthDate;

    @NonNull
    private Gender gender;

}
