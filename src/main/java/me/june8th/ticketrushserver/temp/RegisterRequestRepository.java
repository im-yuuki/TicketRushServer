package me.june8th.ticketrushserver.temp;

import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisterRequestRepository extends KeyValueRepository<RegisterRequest, String> {

    Optional<RegisterRequest> findByKey(String key);

    Optional<RegisterRequest> findByEmail(String email);

}
