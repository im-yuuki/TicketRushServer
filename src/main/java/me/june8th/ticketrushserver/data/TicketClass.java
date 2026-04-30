package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.june8th.ticketrushserver.types.Currency;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sales_round_id", nullable = false)
    private SalesRound salesRound;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Boolean isStandingArea;

    @Column(nullable = false)
    @Builder.Default
    private Integer rows = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer seatsPerRow = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxPurchaseLimit = 0;

}
