package com.taskflow.controller;

import com.taskflow.dto.AcceptInviteRequest;
import com.taskflow.dto.CreateTeamRequest;
import com.taskflow.dto.InviteUserRequest;
import com.taskflow.dto.TeamDto;
import com.taskflow.entity.User;
import com.taskflow.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Team Management endpoints.
 * All endpoints here are protected by default (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * POST /api/v1/teams
     * Creates a new team. The authenticated user becomes the admin.
     */
    @PostMapping
    public ResponseEntity<TeamDto> createTeam(
        @Valid @RequestBody CreateTeamRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        TeamDto createdTeam = teamService.createTeam(request, currentUser);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/teams
     * Lists all teams the authenticated user belongs to.
     */
    @GetMapping
    public ResponseEntity<List<TeamDto>> getUserTeams(
        @AuthenticationPrincipal User currentUser
    ) {
        List<TeamDto> teams = teamService.getTeamsForUser(currentUser);
        return ResponseEntity.ok(teams);
    }

    /**
     * POST /api/v1/teams/{id}/invite
     * Invites a user to the team. (Admin only)
     * (PRD Sec 8.3)
     */
    @PostMapping("/{id}/invite")
    public ResponseEntity<Void> inviteUserToTeam(
        @PathVariable Long id,
        @Valid @RequestBody InviteUserRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        teamService.inviteUser(id, request, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/v1/teams/invite/accept
     * Accepts a team invitation using a token.
     * The authenticated user will be added to the team.
     */
    @PostMapping("/invite/accept")
    public ResponseEntity<TeamDto> acceptTeamInvite(
        @Valid @RequestBody AcceptInviteRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        TeamDto joinedTeam = teamService.acceptInvite(request, currentUser);
        return ResponseEntity.ok(joinedTeam);
    }

    // We will add GET /api/v1/teams/{id}/members, etc., later.
}
