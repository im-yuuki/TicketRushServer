package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.SeatRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRowRepository extends JpaRepository<SeatRow, Long> {

}
