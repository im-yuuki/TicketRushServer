package me.june8th.ticketrushserver.data;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.View;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "administrators")
public class AdministratorAccount extends Account {

    @Override
    public Role getRole() {
        return Role.ADMINISTRATOR;
    }

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Private.class)
    private String avatarKey = null;

}
