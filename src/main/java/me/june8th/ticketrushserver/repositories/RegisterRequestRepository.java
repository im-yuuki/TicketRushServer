package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.RegisterRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisterRequestRepository extends CrudRepository<RegisterRequest, String> {

    Optional<RegisterRequest> findByKey(String key);

    Optional<RegisterRequest> findByEmail(String email);

    void deleteByKey(String key);

    void deleteByEmail(String email);

}
