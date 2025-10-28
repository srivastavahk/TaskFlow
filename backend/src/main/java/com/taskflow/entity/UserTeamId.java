package com.taskflow.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Composite Primary Key class for UserTeam entity.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTeamId implements Serializable {

    private Long user;
    private Long team;
}
