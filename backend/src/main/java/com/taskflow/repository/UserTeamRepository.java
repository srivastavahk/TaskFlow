package com.taskflow.repository;

import com.taskflow.entity.UserTeam;
import com.taskflow.entity.UserTeamId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the UserTeam join entity.
 */
@Repository
public interface UserTeamRepository
    extends JpaRepository<UserTeam, UserTeamId> {
    /**
     * Finds all team memberships for a given user.
     */
    List<UserTeam> findByUserId(Long userId);

    /**
     * Finds all user memberships for a given team.
     */
    List<UserTeam> findByTeamId(Long teamId);

    /**
     * Finds a specific user's membership details for a specific team.
     */
    Optional<UserTeam> findByUserIdAndTeamId(Long userId, Long teamId);
}
