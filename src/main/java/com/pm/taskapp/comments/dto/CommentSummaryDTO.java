package com.pm.taskapp.comments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for comment summary response.
 * Lightweight version for lists and nested responses.
 *
 * <p>
 * Contains essential comment information:
 * <ul>
 * <li>Basic identification (id, content)</li>
 * <li>Author summary (for display)</li>
 * <li>Creation timestamp</li>
 * </ul>
 *
 * <p>
 * Used for:
 * <ul>
 * <li>Comment lists (GET /issues/{issueId}/comments)</li>
 * <li>Activity feeds</li>
 * <li>Nested in other responses</li>
 * </ul>
 *
 * <p>
 * Use {@link CommentResponseDTO} for complete comment details.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentSummaryDTO {

    private UUID id;

    private String content;

    private UserSummaryDTO author;

    private Instant createdAt;

    private Instant updatedAt;

    /**
     * Check if comment has been edited.
     */
    public boolean isEdited() {
        return updatedAt != null && !updatedAt.equals(createdAt);
    }
}