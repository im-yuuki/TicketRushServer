package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
