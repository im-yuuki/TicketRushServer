package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.OrganizationAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationAccountRepository extends JpaRepository<OrganizationAccount, Long> {

    Optional<OrganizationAccount> findByAliasName(String alias);

    boolean existsByAliasName(String alias);

}
