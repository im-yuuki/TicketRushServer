package me.june8th.ticketrushserver.database;

import me.june8th.ticketrushserver.data.SalesRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesRoundRepository extends JpaRepository<SalesRound, Long> {

    List<SalesRound> findAllByEvent_IdOrderByStartTimeAscIdAsc(long eventId);

}
