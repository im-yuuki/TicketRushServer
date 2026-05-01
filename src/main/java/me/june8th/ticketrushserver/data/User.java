package me.june8th.ticketrushserver.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Country;
import me.june8th.ticketrushserver.types.Gender;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends Account {

    @Column(nullable = false)
    private Date birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column
    @Builder.Default
    @Nullable
    private String phoneNumber = null;

    @Column(nullable = false)
    private String addressLine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

}
