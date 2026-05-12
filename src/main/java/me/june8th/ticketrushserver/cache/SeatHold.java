package me.june8th.ticketrushserver.cache;

import org.springframework.data.redis.core.TimeToLive;

public class SeatHold {

    @TimeToLive
    private Long ttl = 900L;

}
