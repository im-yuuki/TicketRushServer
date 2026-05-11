package me.june8th.ticketrushserver.security;

import lombok.Builder;
import me.june8th.ticketrushserver.types.Role;

@Builder
public record AccessTokenData(Long id, Role type, String domain, Integer version) {

    public String toAccessTokenSubject() {
        return type.toString() + "-d" + domain + "-" + id;
    }

}
