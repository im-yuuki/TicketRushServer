package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

}
