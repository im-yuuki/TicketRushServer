package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RequiredArgsConstructor
public enum Role {

    USER, STAFF, ORGANIZATION, ADMINISTRATOR;

    @JsonCreator
    public static Role fromString(String value) {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }

    public SimpleGrantedAuthority toRoleAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + name().toUpperCase());
    }

}
