package me.june8th.ticketrushserver.temp;

import org.springframework.data.redis.core.TimeToLive;

public class SeatHold {

    @TimeToLive
    private Long ttl = 900L;

}
