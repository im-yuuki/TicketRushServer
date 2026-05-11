package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RequiredArgsConstructor
public enum Role {

    USER("user"), INSPECTOR("inspector"), ORGANIZATION("organization"), ADMINISTRATOR("administrator");

    private final String value;

    @JsonCreator
    public static Role fromString(String value) {
        for (Role type : values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid JWT token type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }

    public SimpleGrantedAuthority toSecurityAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + name().toUpperCase());
    }

}
