package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    ArrayList<Event> findAllByOrganization(OrganizationAccount org);

}
