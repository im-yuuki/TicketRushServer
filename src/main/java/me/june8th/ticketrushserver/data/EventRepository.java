package me.june8th.ticketrushserver.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RedisHash("event")
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOwner(ManagerAccount owner);
}
