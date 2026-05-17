package me.june8th.ticketrushserver.database;

import me.june8th.ticketrushserver.data.SeatZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatZoneRepository extends JpaRepository<SeatZone, Long> {

}
