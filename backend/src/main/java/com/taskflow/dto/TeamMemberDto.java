package com.taskflow.dto;

import com.taskflow.entity.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDto {

    private Long userId;
    private String name;
    private String email;
    private TeamRole role;
}
