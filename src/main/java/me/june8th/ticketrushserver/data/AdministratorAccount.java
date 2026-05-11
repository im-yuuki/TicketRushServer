package me.june8th.ticketrushserver.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "administrators")
public class AdministratorAccount extends Account {

    @Override
    public Role getType() {
        return Role.ADMINISTRATOR;
    }

    @Column
    @Builder.Default
    @Nullable
    private String avatarKey = null;

}
