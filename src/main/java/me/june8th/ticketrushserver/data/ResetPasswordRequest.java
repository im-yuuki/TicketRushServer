package me.june8th.ticketrushserver.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@RedisHash(value = "reset_password_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

	@Id
	private String token;

	private Long userId;

	private String email;

	@Builder.Default
	private Instant createdAt = Instant.now();

	@TimeToLive
	@Builder.Default
	private Long ttlSeconds = 3600L;

}
