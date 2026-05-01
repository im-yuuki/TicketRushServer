package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "organizations")
public class Organization extends Account {

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

}
