package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "inspectors")
public class InspectorAccount extends Account {

    @Override
    public Role getType() {
        return Role.INSPECTOR;
    }

    @Override
    public String getDomain() {
        // domain = event id
        return event.getId().toString();
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    private Event event;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    private OrganizationAccount organization;

    @Column(nullable = false)
    @Builder.Default
    private String description = "";

}
