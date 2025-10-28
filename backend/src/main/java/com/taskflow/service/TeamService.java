package com.taskflow.service;

import com.taskflow.dto.CreateTeamRequest;
import com.taskflow.dto.TeamDto;
import com.taskflow.dto.TeamMemberDto;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.Team;
import com.taskflow.entity.TeamRole;
import com.taskflow.entity.User;
import com.taskflow.entity.UserTeam;
import com.taskflow.repository.TeamRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.UserTeamRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository; // To fetch users if needed

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
        // 1. Find all team memberships for the user
        List<UserTeam> memberships = userTeamRepository.findByUserId(
            user.getId()
        );

        // 2. Map the results to TeamDto
        return memberships
            .stream()
            .map(UserTeam::getTeam)
            .map(this::mapTeamToDto)
            .collect(Collectors.toList());
    }

    // --- Helper Methods ---

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
