package me.june8th.ticketrushserver.temp;

import lombok.*;
import me.june8th.ticketrushserver.utils.RandomGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.Date;

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

    @Builder.Default
    private Instant nextResendAvailable = Instant.now();

    @Builder.Default
    private Integer availableAttempts = 5;

    @Indexed
    private String email;

    private String name;

    private String passwordHash;

    private Date birthDate;

    private String genderString;

}
