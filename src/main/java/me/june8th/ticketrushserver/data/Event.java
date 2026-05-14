package me.june8th.ticketrushserver.data;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import me.june8th.ticketrushserver.views.Patchable;
import me.june8th.ticketrushserver.views.Public;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

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
    private long id;

    @Column(nullable = false)
    @JsonView({Public.class, Patchable.class})
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false, updatable = false)
    @JsonView(Public.class)
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

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;

}
