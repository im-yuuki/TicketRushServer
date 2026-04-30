package me.june8th.ticketrushserver.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "register_account_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterAccountRequest {

}
