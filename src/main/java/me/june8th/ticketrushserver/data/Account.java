package me.june8th.ticketrushserver.data;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.Patchable;
import me.june8th.ticketrushserver.utils.View;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Long id;

    @Column(nullable = false)
    @JsonView({View.Public.class, Patchable.class})
    private String name;

    @Column(nullable = false, unique = true)
    @JsonView({View.Shared.class, Patchable.class})
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    @JsonView(View.Private.class)
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    @JsonView(View.Private.class)
    private Instant updatedAt;

    @Transient
    public abstract Role getRole();

    @Transient
    public String getDomain() {
        return getId().toString();
    }

}
