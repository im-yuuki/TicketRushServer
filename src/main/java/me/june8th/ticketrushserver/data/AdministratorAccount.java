package me.june8th.ticketrushserver.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "administrators")
public class AdministratorAccount extends Account {

    @Override
    public Role getRole() {
        return Role.ADMINISTRATOR;
    }

}
