package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false, updatable = false)
    private SeatRow seatRow;

    @Column(nullable = false, updatable = false)
    private int index;

    @Column(nullable = false, updatable = false)
    private int number;

    @OneToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn
    @Builder.Default
    private Ticket associatedTicket = null;

}
