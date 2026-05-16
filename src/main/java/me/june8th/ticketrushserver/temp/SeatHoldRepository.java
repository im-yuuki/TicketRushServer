package me.june8th.ticketrushserver.temp;

import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatHoldRepository extends KeyValueRepository<SeatHold, Long> {

}
