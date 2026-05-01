package me.june8th.ticketrushserver.security;

import lombok.Builder;
import me.june8th.ticketrushserver.types.AccountType;

@Builder
public record AccessTokenData(Long id, AccountType type, String domain, Integer version) {

    public String getPrincipal() {
        return "account-" + id;
    }

}
