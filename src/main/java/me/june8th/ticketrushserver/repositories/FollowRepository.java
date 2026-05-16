package me.june8th.ticketrushserver.repositories;

import me.june8th.ticketrushserver.data.Follow;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndOrganization(UserAccount follower, OrganizationAccount organization);

    Optional<Follow> findByFollowerAndOrganization(UserAccount follower, OrganizationAccount organization);

    List<Follow> findAllByFollowerOrderByAtDescIdDesc(UserAccount follower);

    long countByOrganization(OrganizationAccount organization);

}
