package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.TicketClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketClassRepository extends JpaRepository<TicketClass, Long> {

    List<TicketClass> findAllBySalesRound_Event_IdOrderBySalesRoundStartTimeAscIdAsc(long eventId);

}
