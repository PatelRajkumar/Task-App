package com.pm.taskapp.comments.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.comments.dto.CommentCreateRequestDTO;
import com.pm.taskapp.comments.dto.CommentResponseDTO;
import com.pm.taskapp.comments.dto.CommentSummaryDTO;
import com.pm.taskapp.comments.dto.CommentUpdateRequestDTO;
import com.pm.taskapp.comments.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Issue Comments", description = "APIs for managing comments on issues")
@SecurityRequirement(name = "bearerAuth")
public class IssueCommentController {

        private final CommentService commentService;

        @PostMapping
        @Operation(summary = "Create Comment", description = "Create a new comment on an issue")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Comment created successfully", content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Issue not found"),
                        @ApiResponse(responseCode = "403", description = "User not authorized to comment on this issue"),
                        @ApiResponse(responseCode = "400", description = "Invalid comment data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<CommentResponseDTO> createComment(@Valid @RequestBody CommentCreateRequestDTO request,
                        @PathVariable @Parameter(description = "issue Id") UUID issueId,
                        @CurrentUser UserPrincipal currentUser) {
                log.info("Received request to create comment on issue: {} by user: {}", issueId,
                                currentUser.getId());
                CommentResponseDTO comment = commentService.createComment(issueId, request,
                                currentUser.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        }

        @PutMapping("/{commentId}")
        @Operation(summary = "Update comment", description = "Update an existing comment. Only the comment author can update comments.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment updated successfully", content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid comment data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not the comment author"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<CommentResponseDTO> updateComment(
                        @Valid @RequestBody CommentUpdateRequestDTO request,
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Comment ID") UUID commentId,
                        @CurrentUser UserPrincipal currentUser) {

                log.info("Updating comment: {} on issue: {} by user: {}", commentId, issueId, currentUser.getId());

                CommentResponseDTO comment = commentService.updateComment(commentId, request, currentUser.getId());

                return ResponseEntity.ok(comment);
        }

        @GetMapping("/{commentId}")
        @Operation(summary = "Get comment by ID", description = "Retrieve a specific comment by its ID. User must be a member of the issue's project.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment retrieved successfully", content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<CommentResponseDTO> getCommentById(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Comment ID") UUID commentId,
                        @CurrentUser UserPrincipal currentUser) {

                log.debug("Getting comment: {} on issue: {} for user: {}", commentId, issueId, currentUser.getId());

                CommentResponseDTO comment = commentService.getCommentById(commentId, currentUser.getId());

                return ResponseEntity.ok(comment);
        }

        @DeleteMapping("/{commentId}")
        @Operation(summary = "Delete comment", description = "Soft delete a comment. Only the comment author can delete comments.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not the comment author"),
                        @ApiResponse(responseCode = "404", description = "Comment not found"),
                        @ApiResponse(responseCode = "410", description = "Comment already deleted")
        })
        public ResponseEntity<Void> deleteComment(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Comment ID") UUID commentId,
                        @CurrentUser UserPrincipal currentUser) {

                log.info("Deleting comment: {} on issue: {} by user: {}", commentId, issueId, currentUser.getId());

                commentService.deleteComment(commentId, currentUser.getId());

                return ResponseEntity.noContent().build();
        }

        @GetMapping
        @Operation(summary = "Get issue comments", description = "Retrieve all comments for an issue (paginated, ordered by creation time DESC). User must be a member of the issue's project.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<Page<CommentSummaryDTO>> getIssueComments(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PageableDefault(size = 20, sort = "createdAt") @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {

                log.debug("Getting comments for issue: {} by user: {}", issueId, currentUser.getId());

                Page<CommentSummaryDTO> comments = commentService.getIssueComments(issueId, currentUser.getId(),
                                pageable);

                return ResponseEntity.ok(comments);
        }

        @GetMapping("/count")
        @Operation(summary = "Count issue comments", description = "Get total number of comments on an issue (excludes deleted). User must be project member.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment count retrieved successfully", content = @Content(schema = @Schema(implementation = Long.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a project member"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<Long> countIssueComments(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @CurrentUser UserPrincipal currentUser) {

                log.debug("Counting comments for issue: {} by user: {}", issueId, currentUser.getId());

                long count = commentService.countIssueComments(issueId, currentUser.getId());

                return ResponseEntity.ok(count);
        }
}