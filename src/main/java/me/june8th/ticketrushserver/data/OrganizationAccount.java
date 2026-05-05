package me.june8th.ticketrushserver.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.AccountType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "organizations")
public class OrganizationAccount extends Account {

    @Override
    public AccountType getType() {
        return AccountType.ORGANIZATION;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    private AdministratorAccount manager;

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
