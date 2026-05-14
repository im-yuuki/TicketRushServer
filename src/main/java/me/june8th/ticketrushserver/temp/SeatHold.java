package me.june8th.ticketrushserver.temp;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("seat_holds")
public class SeatHold {

    @TimeToLive
    private Long ttl = 900L;

    @Id
    private Long seatId;

    @Indexed
    private Long seatZoneId;

    @Indexed
    private Long eventId;

    @Indexed
    private Long userId;

}
