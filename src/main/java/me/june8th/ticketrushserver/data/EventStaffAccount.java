package me.june8th.ticketrushserver.data;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.views.Shared;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "event_staffs")
public class EventStaffAccount extends Account {

    @Override
    public Role getRole() {
        return Role.STAFF;
    }

    // domain = event id
    @Override
    public String getDomain() {
        return String.valueOf(event.getId());
    }

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false, updatable = false)
    @JsonView(Shared.class)
    private Event event;

}
