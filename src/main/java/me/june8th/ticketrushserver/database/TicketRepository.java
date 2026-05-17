package me.june8th.ticketrushserver.database;

import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.data.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByUserOrderByCreatedAtDesc(UserAccount user);

    @Query("""
            select e.id from Ticket t
            join t.ticketClass tc
            join tc.salesRound sr
            join sr.event e
            where e.published = true and e.dateTime > :now
            group by e.id, e.dateTime
            order by count(t.id) desc, e.dateTime asc, e.id asc
            """)
    List<Long> findTrendingEventIds(@Param("now") Instant now);

    @Query("""
            select e.organization.id from Ticket t
            join t.ticketClass tc
            join tc.salesRound sr
            join sr.event e
            where t.user.id = :userId
            group by e.organization.id
            order by max(t.createdAt) desc, e.organization.id asc
            """)
    List<Long> findPurchasedOrganizationIdsByUserId(@Param("userId") long userId);

}
