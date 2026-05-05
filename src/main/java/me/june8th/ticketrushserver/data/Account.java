package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.AccountType;

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
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Transient
    public abstract AccountType getType();

    @Transient
    public String getDomain() {
        return getType().toString();
    }

}
