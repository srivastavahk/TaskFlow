package com.taskflow.service;

import com.taskflow.dto.AcceptInviteRequest;
import com.taskflow.dto.CreateTeamRequest;
import com.taskflow.dto.InviteUserRequest;
import com.taskflow.dto.TeamDto;
import com.taskflow.dto.TeamMemberDto;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.Invitation;
import com.taskflow.entity.Team;
import com.taskflow.entity.TeamRole;
import com.taskflow.entity.TeamRole;
import com.taskflow.entity.User;
import com.taskflow.entity.UserTeam;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.InvitationRepository;
import com.taskflow.repository.TeamRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.UserTeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository; // To fetch users if needed
    private final InvitationRepository invitationRepository;

    /**
     * Creates a new team and assigns the creator as the ADMIN.
     *
     * @param request      The DTO containing team details.
     * @param creator      The authenticated user creating the team.
     * @return A DTO of the newly created team.
     */
    @Transactional
    public TeamDto createTeam(CreateTeamRequest request, User creator) {
        // 1. Create and save the Team entity
        Team team = Team.builder()
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(creator)
            .build();
        Team savedTeam = teamRepository.save(team);

        // 2. Add the creator as the first member with ADMIN role
        UserTeam membership = UserTeam.builder()
            .team(savedTeam)
            .user(creator)
            .role(TeamRole.ROLE_ADMIN)
            .build();
        userTeamRepository.save(membership);

        // 3. Return the DTO
        return mapTeamToDto(savedTeam);
    }

    /**
     * Gets all teams that the specified user is a member of.
     *
     * @param user The authenticated user.
     * @return A list of TeamDto.
     */
    @Transactional(readOnly = true)
    public List<TeamDto> getTeamsForUser(User user) {
        // // 1. Find all team memberships for the user
        // List<UserTeam> memberships = userTeamRepository.findByUserId(
        //     user.getId()
        // );

        // // 2. Map the results to TeamDto
        // return memberships
        //     .stream()
        //     .map(UserTeam::getTeam)
        //     .map(this::mapTeamToDto)
        //     .collect(Collectors.toList());
        return userTeamRepository
            .findByUserId(user.getId())
            .stream()
            .map(membership -> mapTeamToDto(membership.getTeam())) // Use the team from the membership
            .collect(Collectors.toList());
    }

    @Transactional
    public void inviteUser(Long teamId, InviteUserRequest request, User admin) {
        // 1. Get team and verify user is an ADMIN
        Team team = getTeamAndVerifyRole(
            teamId,
            admin.getId(),
            TeamRole.ROLE_ADMIN
        );

        // 2. Check if user is already a member of the team
        userRepository
            .findByEmail(request.getEmail())
            .ifPresent(user -> {
                if (
                    userTeamRepository
                        .findByUserIdAndTeamId(user.getId(), teamId)
                        .isPresent()
                ) {
                    throw new DuplicateResourceException(
                        "User is already a member of this team"
                    );
                }
            });

        // 3. Check if an active invitation already exists
        if (
            invitationRepository.existsByEmailAndTeamId(
                request.getEmail(),
                teamId
            )
        ) {
            // In a real app, we might resend the existing invite
            throw new DuplicateResourceException(
                "An invitation for this email already exists"
            );
        }

        // 4. Create and save the invitation token
        String token = UUID.randomUUID().toString();
        Invitation invitation = Invitation.builder()
            .email(request.getEmail())
            .team(team)
            .invitedBy(admin)
            .token(token)
            .expiresAt(LocalDateTime.now().plusDays(7)) // 7-day expiry
            .build();

        invitationRepository.save(invitation);

        // 5. Simulate sending the email
        // In a real app, this would be an async email service call.
        log.info("--- SIMULATED EMAIL ---");
        log.info("To: {}", request.getEmail());
        log.info("From: no-reply@taskflow.com");
        log.info(
            "Subject: You're invited to join {} on TaskFlow!",
            team.getName()
        );
        log.info(
            "Body: Click here to accept: http://localhost:3000/invite?token={}",
            token
        );
        log.info("-----------------------");
    }

    /**
     * Accepts an invitation and adds the user to the team.
     */
    @Transactional
    public TeamDto acceptInvite(AcceptInviteRequest request, User user) {
        // 1. Find the invitation by token
        Invitation invitation = invitationRepository
            .findByToken(request.getToken())
            .orElseThrow(() ->
                new ResourceNotFoundException("Invitation", "token", "invalid")
            );

        // 2. Validate the invitation
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitationRepository.delete(invitation); // Clean up expired token
            throw new AccessDeniedException("Invitation has expired");
        }

        // 3. Check that the email matches the authenticated user
        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new AccessDeniedException(
                "This invitation is for a different email address"
            );
        }

        // 4. Check if user is already a member
        if (
            userTeamRepository
                .findByUserIdAndTeamId(
                    user.getId(),
                    invitation.getTeam().getId()
                )
                .isPresent()
        ) {
            invitationRepository.delete(invitation); // Token is used, delete it
            throw new DuplicateResourceException(
                "You are already a member of this team"
            );
        }

        // 5. Add user to the team with MEMBER role
        UserTeam membership = UserTeam.builder()
            .team(invitation.getTeam())
            .user(user)
            .role(TeamRole.ROLE_MEMBER) // New members are Members
            .build();
        userTeamRepository.save(membership);

        // 6. Delete the invitation so it can't be reused
        invitationRepository.delete(invitation);

        log.info(
            "User {} successfully joined team {}",
            user.getEmail(),
            invitation.getTeam().getName()
        );

        // 7. Return the team DTO
        return mapTeamToDto(invitation.getTeam());
    }

    // --- Helper Methods ---

    /**
     * Finds a team and verifies the user has the required role.
     * @return The Team entity if successful.
     * @throws ResourceNotFoundException if team not found.
     * @throws AccessDeniedException if user is not a member or lacks the role.
     */
    private Team getTeamAndVerifyRole(
        Long teamId,
        Long userId,
        TeamRole requiredRole
    ) {
        Team team = teamRepository
            .findById(teamId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Team", "id", teamId)
            );

        UserTeam membership = userTeamRepository
            .findByUserIdAndTeamId(userId, teamId)
            .orElseThrow(() ->
                new AccessDeniedException("User is not a member of this team")
            );

        if (membership.getRole() != requiredRole) {
            // For now, only check for exact role.
            // We could expand this to hierarchical roles (e.g., ADMIN > MEMBER)
            throw new AccessDeniedException(
                "User does not have required permissions: " + requiredRole
            );
        }

        return team;
    }

    private TeamDto mapTeamToDto(Team team) {
        return TeamDto.builder()
            .id(team.getId())
            .name(team.getName())
            .description(team.getDescription())
            .createdAt(team.getCreatedAt())
            .createdBy(mapUserToDto(team.getCreatedBy()))
            .members(getTeamMembers(team.getId())) // Fetch members
            .build();
    }

    private Set<TeamMemberDto> getTeamMembers(Long teamId) {
        return userTeamRepository
            .findByTeamId(teamId)
            .stream()
            .map(this::mapUserTeamToMemberDto)
            .collect(Collectors.toSet());
    }

    private TeamMemberDto mapUserTeamToMemberDto(UserTeam userTeam) {
        User user = userTeam.getUser();
        return TeamMemberDto.builder()
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(userTeam.getRole())
            .build();
    }

    private UserDto mapUserToDto(User user) {
        return UserDto.builder()
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .status(user.getStatus())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
