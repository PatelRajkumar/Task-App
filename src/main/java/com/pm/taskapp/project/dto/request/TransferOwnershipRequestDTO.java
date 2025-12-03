package com.pm.taskapp.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * DTO for transferring project ownership.
 * 
 * <p>Validation rules:
 * <ul>
 *   <li>New owner user ID: required, must be valid UUID</li>
 * </ul>
 * 
 * <p>Business rules:
 * <ul>
 *   <li>New owner must already be a project member</li>
 *   <li>Current user must be an OWNER</li>
 *   <li>Cannot transfer to self</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOwnershipRequestDTO {

    /**
     * ID of the user who will become the new owner.
     * Must already be a member of the project.
     */
    @NotNull(message = "New owner user ID is required")
    private UUID newOwnerUserId;
}