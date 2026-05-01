package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.ResetPasswordRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordRequestRepository extends CrudRepository<ResetPasswordRequest, String> {

    Optional<ResetPasswordRequest> findByKey(String key);

    Optional<ResetPasswordRequest> findByUserId(Long userId);

    Optional<ResetPasswordRequest> findByEmail(String email);

    void deleteByKey(String key);

    void deleteByUserId(Long userId);

    void deleteByEmail(String email);

}
