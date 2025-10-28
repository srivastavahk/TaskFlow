package com.taskflow.dto;

import com.taskflow.entity.UserStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A standard DTO for representing User information in API responses.
 * Used in registration response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;
    private String name;
    private String email;
    private UserStatus status;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
