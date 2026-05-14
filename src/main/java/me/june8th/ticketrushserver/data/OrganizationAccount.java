package me.june8th.ticketrushserver.data;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.views.View;

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
    @JsonView(View.Public.class)
    private Boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    @JsonView(View.Public.class)
    private String description = "";

    @Column(unique = true)
    @Builder.Default
    @Nullable
    @JsonView(View.Public.class)
    private String aliasName = null;

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Public.class)
    private String avatarKey = null;

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Public.class)
    private String bannerKey = null;

    @Column
    @Builder.Default
    @Nullable
    @JsonView(View.Public.class)
    private String websiteUrl = null;

}
