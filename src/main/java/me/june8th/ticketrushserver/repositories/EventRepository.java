package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.ManagerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOwner(ManagerAccount owner);
}
