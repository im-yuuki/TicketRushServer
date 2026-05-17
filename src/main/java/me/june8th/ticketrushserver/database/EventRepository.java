package me.june8th.ticketrushserver.database;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    ArrayList<Event> findAllByOrganization(OrganizationAccount org);

    List<Event> findAllByPublishedTrueAndDateTimeAfterOrderByDateTimeAscIdAsc(Instant now);

}
