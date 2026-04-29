package me.june8th.ticketrushserver.types;

import java.time.LocalDateTime;

public interface Account {

    Long getId();

    String getName();

    String getEmail();

    String getPasswordHash();

    String getTokenVersion();

    LocalDateTime getCreatedAt();

    Boolean getAccountNonLocked();

    Role getRole();

}
