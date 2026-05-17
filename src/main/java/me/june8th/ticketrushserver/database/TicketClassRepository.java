package me.june8th.ticketrushserver.database;

import me.june8th.ticketrushserver.data.TicketClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TicketClassRepository extends JpaRepository<TicketClass, Long> {

    List<TicketClass> findAllBySalesRound_Event_IdOrderBySalesRoundStartTimeAscIdAsc(long eventId);

    @Query("""
            select tc.salesRound.event.id as eventId, min(tc.price) as minimumTicketPrice
            from TicketClass tc
            where tc.salesRound.event.id in :eventIds
            group by tc.salesRound.event.id
            """)
    List<EventMinimumTicketPrice> findMinimumPricesByEventIds(@Param("eventIds") Collection<Long> eventIds);

    interface EventMinimumTicketPrice {
        long getEventId();

        Long getMinimumTicketPrice();
    }

}
