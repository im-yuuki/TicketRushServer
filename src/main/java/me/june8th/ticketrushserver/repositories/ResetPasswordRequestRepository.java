package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.temp.ResetPasswordRequest;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordRequestRepository extends KeyValueRepository<ResetPasswordRequest, String> {

    Optional<ResetPasswordRequest> findByKey(String key);

    Optional<ResetPasswordRequest> findByEmail(String email);

}
