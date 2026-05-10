package me.june8th.ticketrushserver.views;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.AccountType;

@Data
@NoArgsConstructor
@SuperBuilder
public class ProfileView {
    private String name;
    private String email;
    private AccountType type;
}

