package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

}
