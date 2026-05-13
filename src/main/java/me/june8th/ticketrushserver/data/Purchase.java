package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false, updatable = false)
    private UserAccount user;

    @Column(nullable = false, updatable = false)
    private Long amount;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant at;

}
