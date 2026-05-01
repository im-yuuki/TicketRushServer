package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.RegisterRequest;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisterRequestRepository extends KeyValueRepository<RegisterRequest, String> {

    Optional<RegisterRequest> findByKey(String key);

    Optional<RegisterRequest> findByEmail(String email);

}
