package com.taskflow.repository;

import com.taskflow.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Team entity.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    // We can add custom query methods here later, e.g.:
    // List<Team> findByCreatedBy(User user);
}
