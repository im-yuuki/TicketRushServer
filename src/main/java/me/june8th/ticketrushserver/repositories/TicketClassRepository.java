package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.TicketClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketClassRepository extends JpaRepository<TicketClass, Long> {

}
