package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.SalesRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesRoundRepository extends JpaRepository<SalesRound, Long> {

}
