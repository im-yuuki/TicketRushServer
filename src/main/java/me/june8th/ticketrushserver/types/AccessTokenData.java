package me.june8th.ticketrushserver.types;

import lombok.Builder;

@Builder
public record AccessTokenData(Long id, Role role, String domain, Integer version) {

    public String toAccessTokenSubject() {
        return role.toString() + "-d" + domain + "-" + id;
    }

}
