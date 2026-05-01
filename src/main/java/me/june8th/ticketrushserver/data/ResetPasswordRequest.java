package me.june8th.ticketrushserver.data;

import lombok.*;
import me.june8th.ticketrushserver.utils.RandomGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@RedisHash(value = "reset_password_request")
@Data
@Builder
public class ResetPasswordRequest {

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
	private Long userId;

    @Indexed
    @NonNull
	private String email;

    @NonNull
    private String newPasswordHash;

}
