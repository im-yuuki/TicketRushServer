package me.june8th.ticketrushserver.types;

import java.time.Instant;

public interface Account {

    Long getId();

    String getName();

    String getEmail();

    String getPasswordHash();

    Integer getTokenVersion();

    Instant getCreatedAt();

    Boolean getAccountNonLocked();

    Role getRole();

}
