package com.pm.taskapp.task.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.common.exception.ErrorResponse;
import com.pm.taskapp.task.dto.IssueHistoryResponseDTO;
import com.pm.taskapp.task.dto.IssueResponseDTO;
import com.pm.taskapp.task.dto.IssueSummaryDTO;
import com.pm.taskapp.task.enums.IssuePriority;
import com.pm.taskapp.task.enums.IssueStatus;
import com.pm.taskapp.task.enums.IssueType;
import com.pm.taskapp.task.service.IssueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Issues", description = "Individual issue operations")
@SecurityRequirement(name = "bearerAuth")
public class IssueController {

        private final IssueService issueService;

        @GetMapping("/{issueId}")
        @Operation(summary = "Get issue by ID", description = "Retrieves a specific issue by its UUID. User must be a member of the issue's project to access it.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue retrieved successfully", content = @Content(schema = @Schema(implementation = IssueResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "User is not a member of the issue's project"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<IssueResponseDTO> getIssueById(
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Getting issue by ID: {} for user: {}", issueId, currentUser.getId());
                IssueResponseDTO issue = issueService.getIssueById(issueId, currentUser.getId());
                return ResponseEntity.ok(issue);
        }

        @GetMapping("/key/{issueKey}")
        @Operation(summary = "Get issue by key", description = "Retrieves a specific issue by its key (e.g., PROJ-123). User must be a member of the issue's project to access it.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue retrieved successfully", content = @Content(schema = @Schema(implementation = IssueResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "User is not a member of the issue's project"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<IssueResponseDTO> getIssueByKey(
                        @PathVariable @Parameter(description = "Issue Key") String issueKey,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Getting issue by key: {} for user: {}", issueKey, currentUser.getId());

                IssueResponseDTO issue = issueService.getIssueByKey(issueKey, currentUser.getId());
                return ResponseEntity.ok(issue);
        }

        @GetMapping("/filter")
        @Operation(summary = "Filter Issues", description = "Filter issues by multiple criteria across all projects the user is a member of. All filter parameters are optional.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issues retrieved successfully", content = @Content(schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
                        @ApiResponse(responseCode = "403", description = "User is not a member of the issue's project"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<Page<IssueSummaryDTO>> filterIssues(

                        @RequestParam(required = false) @Parameter(description = "Filter by project") UUID projectId,

                        @RequestParam(required = false) @Parameter(description = "Filter by reporter") UUID reporterId,

                        @RequestParam(required = false) @Parameter(description = "Filter by status (TODO, INPROGRESS, DONE)") IssueStatus status,

                        @RequestParam(required = false) @Parameter(description = "Filter by type (BUG, FEATURE, TASK, ENHANCEMENT)") IssueType type,

                        @RequestParam(required = false) @Parameter(description = "Filter by priority (LOW, MEDIUM, HIGH, CRITICAL)") IssuePriority priority,

                        @RequestParam(required = false) @Parameter(description = "Filter by assignee user ID") UUID assigneeId,

                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Filter issues for user: {}", currentUser.getId());
                Page<IssueSummaryDTO> issues = issueService.filterIssues(projectId, status, type, priority, assigneeId,
                                reporterId, currentUser.getId(), pageable);

                return ResponseEntity.ok(issues);
        }

        @GetMapping("/my-assigned")
        @Operation(summary = "Get my assigned issues", description = "Retrieves all issues assigned to the authenticated user across all projects. "
                        +
                        "Returns issues from all projects where the user is assigned, regardless of project membership. "
                        +
                        "Results are paginated and sorted by creation date (newest first) by default.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Assigned issues retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required. User must be logged in to access their assigned issues.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<Page<IssueSummaryDTO>> getMyAssignedIssues(
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Get all issue assigned to current user: {}", currentUser.getId());

                Page<IssueSummaryDTO> issues = issueService.getMyAssignedIssues(currentUser.getId(), pageable);
                return ResponseEntity.ok(issues);
        }

        @GetMapping("/my-reported")
        @Operation(summary = "Get my reported issues", description = "Retrieves all issues reported by the authenticated user across all projects. "
                        +
                        "Returns issues from all projects where the user is reported, regardless of project membership. "
                        +
                        "Results are paginated and sorted by creation date (newest first) by default.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Reported issues retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueSummaryDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required. User must be logged in to access their assigned issues.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<Page<IssueSummaryDTO>> getMyReportedIssues(
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Get all issue reported by current user: {}", currentUser.getId());

                Page<IssueSummaryDTO> issues = issueService.getMyReportedIssues(currentUser.getId(), pageable);
                return ResponseEntity.ok(issues);
        }

        @GetMapping("/{issueId}/history")
        @Operation(summary = "Get issue history", description = "Retrieves the complete audit trail for a specific issue. "
                        +
                        "User must be a member of the issue's project to view its history.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Issue history retrieved successfully", content = @Content(schema = @Schema(implementation = IssueHistoryResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a project member"),
                        @ApiResponse(responseCode = "404", description = "Issue not found")
        })
        public ResponseEntity<Page<IssueHistoryResponseDTO>> getIssueHistory(
                        @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
                        @PathVariable @Parameter(description = "Issue ID") UUID issueId,
                        @CurrentUser UserPrincipal currentUser) {
                log.debug("Get issue history of {} for user: {}", issueId, currentUser.getId());

                Page<IssueHistoryResponseDTO> issueHistory = issueService.getIssueHistory(issueId, currentUser.getId(),
                                pageable);
                return ResponseEntity.ok(issueHistory);
        }

        @GetMapping("/my-assigned/count")
        @Operation(summary = "Get my assigned issues count", description = "Returns the total count of active issues assigned to the authenticated user across all projects.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Assigned issues count retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class), examples = @ExampleObject(value = "15"))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
        })
        public ResponseEntity<Long> getMyAssignedIssuesCount(
                        @CurrentUser @Parameter(hidden = true) UserPrincipal currentUser) {

                log.debug("Getting assigned issues count for user: {}", currentUser.getId());

                long count = issueService.countMyAssignedIssues(currentUser.getId());

                return ResponseEntity.ok(count);
        }
}
