package me.june8th.ticketrushserver.data;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.utils.View;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class UserAccount extends Account {

    @Override
    public Role getRole() {
        return Role.USER;
    }

    @Column(nullable = false)
    @JsonView(View.Private.class)
    private Date birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(View.Private.class)
    private Gender gender;

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Shared.class)
    private String avatarKey = null;

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Private.class)
    private String phoneNumber = null;

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Private.class)
    private String addressLine = null;

}
