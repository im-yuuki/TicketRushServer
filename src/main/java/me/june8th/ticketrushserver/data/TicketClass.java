package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
