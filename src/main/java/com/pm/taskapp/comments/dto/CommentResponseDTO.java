package com.pm.taskapp.comments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.taskapp.project.dto.response.UserSummaryDTO;
import com.pm.taskapp.task.dto.IssueSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for complete comment response.
 * Contains full comment information with related entity summaries.
 *
 * <p>
 * Includes:
 * <ul>
 * <li>Basic comment data (id, content, timestamps)</li>
 * <li>Issue summary (key, title)</li>
 * <li>Author summary (name, email, avatar)</li>
 * <li>Deletion status tracking</li>
 * </ul>
 *
 * <p>
 * Used for:
 * <ul>
 * <li>Single comment retrieval (GET /comments/{id})</li>
 * <li>After creating a comment (POST /comments)</li>
 * <li>After updating a comment (PUT /comments/{id})</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponseDTO {

    private UUID id;

    private String content;

    private IssueSummaryDTO issue;

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