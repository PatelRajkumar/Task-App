package com.pm.taskapp.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

/**
 * DTO for simplified user information.
 * Used in responses where full user details are not needed.
 * 
 * <p>Contains only essential user information:
 * <ul>
 *   <li>ID for identification</li>
 *   <li>Name for display</li>
 *   <li>Email for contact</li>
 *   <li>Avatar URL for UI</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryDTO {

    /**
     * User unique identifier.
     */
    private UUID id;

    /**
     * User full name.
     */
    private String name;

    /**
     * User email address.
     */
    private String email;

    /**
     * User avatar URL.
     * Optional.
     */
    private String avatarUrl;
}