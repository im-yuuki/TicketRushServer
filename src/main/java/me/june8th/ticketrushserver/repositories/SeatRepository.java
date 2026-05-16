package me.june8th.ticketrushserver.repositories;

import jakarta.persistence.LockModeType;
import me.june8th.ticketrushserver.data.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :seatIds order by s.id")
    List<Seat> findAllByIdInForUpdate(@Param("seatIds") Collection<Long> seatIds);

    @Query("""
            select s from Seat s
            join fetch s.seatRow sr
            join fetch sr.seatZone sz
            left join fetch s.associatedTicket t
            where sz.event.id = :eventId
            order by sz.id, sr.index, s.number
            """)
    List<Seat> findAllByEventId(@Param("eventId") long eventId);

}
