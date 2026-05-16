package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.data.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByUserOrderByCreatedAtDesc(UserAccount user);

}
