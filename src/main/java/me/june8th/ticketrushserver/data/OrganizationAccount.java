package me.june8th.ticketrushserver.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "organizations")
public class OrganizationAccount extends Account {

    @Override
    public Role getRole() {
        return Role.ORGANIZATION;
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private String description = "";

    @Column(unique = true)
    @Builder.Default
    @Nullable
    private String aliasName = null;

    @Column
    @Builder.Default
    @Nullable
    private String avatarKey = null;

    @Column
    @Builder.Default
    @Nullable
    private String bannerKey = null;

    @Column
    @Builder.Default
    @Nullable
    private String websiteUrl = null;

}
