package com.pm.taskapp.task.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.task.dto.IssueAssignRequestDTO;
import com.pm.taskapp.task.dto.IssueCreateRequestDTO;
import com.pm.taskapp.task.dto.IssueHistoryResponseDTO;
import com.pm.taskapp.task.dto.IssueResponseDTO;
import com.pm.taskapp.task.dto.IssueSummaryDTO;
import com.pm.taskapp.task.dto.IssueUpdateRequestDTO;
import com.pm.taskapp.task.dto.IssueUpdateStatusRequestDTO;
import com.pm.taskapp.task.service.IssueService;

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
@RequestMapping("/api/projects/{projectId}/issues")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Issues", description = "Project-scoped issue operations")
@SecurityRequirement(name = "bearerAuth")
public class ProjectIssueController {

        private final IssueService issueService;

        @PostMapping
        @Operation(summary = "Create new issue")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Issue created successfully", content = @Content(schema = @Schema(implementation = IssueResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<IssueResponseDTO> createIssue(@Valid @RequestBody IssueCreateRequestDTO request,
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @CurrentUser UserPrincipal currentUser) {
                log.info("Creating issue '{}' of project '{}' by user '{}'", request.getTitle(), projectId,
                                currentUser.getId());

                IssueResponseDTO issue = issueService.createIssue(projectId, request, currentUser.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(issue);
        }

        @PutMapping("/{issueId}")
        @Operation(summary = "Update issue")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue updated successfully", content = @Content(schema = @Schema(implementation = IssueResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<IssueResponseDTO> updateIssue(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @Valid @RequestBody IssueUpdateRequestDTO requestDTO,
                        @CurrentUser UserPrincipal currentUser) {
                IssueResponseDTO issue = issueService.updateIssue(issueId, projectId, requestDTO, currentUser.getId());
                return ResponseEntity.status(HttpStatus.OK).body(issue);
        }

        @PutMapping("/{issueId}/status")
        @Operation(summary = "Update issue status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue status updated successfully", content = @Content(schema = @Schema(implementation = IssueResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<IssueResponseDTO> updateIssueStatus(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @Valid @RequestBody IssueUpdateStatusRequestDTO requestDTO,
                        @CurrentUser UserPrincipal currentUser) {
                IssueResponseDTO issue = issueService.updateIssueStatus(issueId, projectId, requestDTO,
                                currentUser.getId());
                return ResponseEntity.status(HttpStatus.OK).body(issue);
        }

        @DeleteMapping("/{issueId}")
        @Operation(summary = "Delete issue", description = "Delete a issue, soft delete.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Issue deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Issue not found"),
                        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Void> deleteIssue(@PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @CurrentUser UserPrincipal currentUser) {
                log.info("Deleting issue '{}' of project '{}' by user '{}'", issueId, projectId, currentUser.getId());
                issueService.deleteIssue(issueId, projectId, currentUser.getId());
                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{issueId}/assign")
        @Operation(summary = "Assign issue to user", description = "Assigns an issue to a specific user. The assignee must be a member of the project. Only the reporter, current assignee, or project OWNER/ADMIN can assign issues.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue assigned successfully", content = @Content(schema = @Schema(implementation = IssueResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input - Assignee not found or not a project member"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User doesn't have permission to edit this issue"),
                        @ApiResponse(responseCode = "404", description = "Issue not found or already deleted")
        })
        public ResponseEntity<IssueResponseDTO> assignIssue(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @Valid @RequestBody IssueAssignRequestDTO requestDTO,
                        @CurrentUser UserPrincipal currentUser) {

                log.info("Processing assignment request for issue: {} in project: {} by user: {}",
                                issueId, projectId, currentUser.getId());

                IssueResponseDTO issue = issueService.assignIssue(issueId, projectId, requestDTO,
                                currentUser.getId());

                return ResponseEntity.ok(issue);
        }

        @GetMapping
        @Operation(summary = "Get all issues of project", description = "Retrieves all the issues from project")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issues retrieved successfully", content = @Content(schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<Page<IssueSummaryDTO>> getProjectIssues(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Getting issues of project: {} for user: {}", projectId, currentUser.getId());
                Page<IssueSummaryDTO> issues = issueService.getProjectIssues(projectId, pageable, currentUser.getId());
                return ResponseEntity.ok(issues);
        }

        @GetMapping("/search")
        @Operation(summary = "Search issues", description = "Search issues by title and any other fields")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issues found", content = @Content(schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Page<IssueSummaryDTO>> searchIssues(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @RequestParam @Parameter(description = "Search term (matches name or key)") String searchTerm,
                        @CurrentUser UserPrincipal currentUser,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters") Pageable pageable) {

                log.debug("Searching issues with term: {} in project: {} for user: {}", searchTerm,
                                projectId, currentUser.getId());
                Page<IssueSummaryDTO> issues = issueService.searchIssues(projectId, searchTerm, pageable,
                                currentUser.getId());
                return ResponseEntity.ok(issues);
        }

        @GetMapping("/unassigned")
        @Operation(summary = "Get unassigned issues", description = "Retrieves all unassigned issues (without an assignee) in a specific project. "
                        +
                        "User must be a member of the project to access these issues. " +
                        "Results are paginated and sorted by creation date (newest first) by default.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Unassigned issues retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Project not found")
        })
        public ResponseEntity<Page<IssueSummaryDTO>> getUnassignedProjectIssues(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Getting unassigned issues in project: {} for user: {}", projectId, currentUser.getId());
                Page<IssueSummaryDTO> issues = issueService.getUnassignedIssues(
                                projectId,
                                currentUser.getId(),
                                pageable);

                return ResponseEntity.ok(issues);
        }

        @GetMapping("/overdue")
        @Operation(summary = "Get overdue issues", description = "Retrieves all overdue issues in a project. " +
                        "An issue is considered overdue if its due date is before today and status is not DONE. " +
                        "Results are sorted by due date (oldest first) and then by priority.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Overdue issues retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Project not found")
        })
        public ResponseEntity<Page<IssueSummaryDTO>> getOverdueIssues(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC) @Parameter(description = "Pagination parameters") Pageable pageable,
                        @RequestParam(required = false) @Parameter(description = "Filter by project") LocalDate asOfDate,
                        @CurrentUser UserPrincipal currentUser) {

                Page<IssueSummaryDTO> issues = issueService.getOverdueIssues(
                                projectId,
                                currentUser.getId(),
                                asOfDate, // Can be null (defaults to today in service)
                                pageable);
                return ResponseEntity.ok(issues);

        }

        @GetMapping("/recently-updated")
        @Operation(summary = "Get recently updated issues", description = "Retrieves all issues in the project sorted by last update time (most recent first). "
                        +
                        "User must be a member of the project to access these issues.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issues retrieved successfully, sorted by recent updates", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Project not found")
        })
        public ResponseEntity<Page<IssueSummaryDTO>> getRecentlyUpdatedIssues(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,
                        @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {
                Page<IssueSummaryDTO> issues = issueService.getRecentlyUpdatedIssues(projectId, currentUser.getId(),
                                pageable);
                return ResponseEntity.ok(issues);
        }

        @GetMapping("/activity")
        @Operation(summary = "Get project activity history", description = "Retrieves the complete activity history for all issues in the project. "
                        +
                        "Shows all changes made to any issue in chronological order. " +
                        "User must be a member of the project to view its activity.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Project activity history retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a member of the project"),
                        @ApiResponse(responseCode = "404", description = "Project not found")
        })
        public ResponseEntity<Page<IssueHistoryResponseDTO>> getProjectActivityHistory(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId,

                        @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) @Parameter Pageable pageable,

                        @CurrentUser @Parameter(hidden = true) UserPrincipal currentUser) {

                log.debug("Getting activity history for project: {} requested by user: {}",
                                projectId, currentUser.getId());

                Page<IssueHistoryResponseDTO> history = issueService.getProjectActivityHistory(
                                projectId,
                                currentUser.getId(),
                                pageable);

                return ResponseEntity.ok(history);
        }

        @GetMapping("/stats")
        @Operation(summary = "Get project issue count", description = "Returns the total count of active issues in the project.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue count retrieved successfully", content = @Content(schema = @Schema(implementation = Long.class))),
                        @ApiResponse(responseCode = "404", description = "Project not found")
        })
        public ResponseEntity<Long> getProjectIssueCount(
                        @PathVariable @Parameter(description = "Project ID") UUID projectId) {

                log.debug("Getting issue count for project: {}", projectId);

                long count = issueService.countProjectIssues(projectId);

                return ResponseEntity.ok(count);
        }
}
