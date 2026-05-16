package me.june8th.ticketrushserver.temp;

import lombok.Builder;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatHoldRepository extends KeyValueRepository<SeatHold, Long> {

}
