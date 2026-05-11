package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ticket_classes")
public class TicketClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    private SalesRound salesRound;

    @Column(nullable = false)
    private Long price; // in vnd

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Boolean isStandingArea;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxPurchaseLimit = 0;

}
