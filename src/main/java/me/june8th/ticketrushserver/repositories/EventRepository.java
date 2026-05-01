package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
