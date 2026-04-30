package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.ResetPasswordRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordRequestRepository extends CrudRepository<ResetPasswordRequest, String> {

    Optional<ResetPasswordRequest> findByToken(String token);

    Optional<ResetPasswordRequest> findByEmail(String email);

    Optional<ResetPasswordRequest> findByUserId(Long userId);

    void deleteByEmail(String email);

    void deleteByUserId(Long userId);
}

