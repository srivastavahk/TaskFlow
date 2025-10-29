package com.taskflow.repository;

import com.taskflow.entity.Invitation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);

    boolean existsByEmailAndTeamId(String email, Long teamId);
}
