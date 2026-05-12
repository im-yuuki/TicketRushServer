package me.june8th.ticketrushserver.data;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    private OrganizationAccount organization;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @Column(nullable = false)
    @Builder.Default
    private String description = "";

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Instant dateTime;

    @Column(nullable = false)
    @Builder.Default
    private String bannerKey = "";

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

}
