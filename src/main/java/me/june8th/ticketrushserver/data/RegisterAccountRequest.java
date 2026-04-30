package me.june8th.ticketrushserver.data;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "register_account_request")
@Data
@NoArgsConstructor
@Builder
public class RegisterAccountRequest {

}
