package com.pm.taskapp.task.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.project.dto.response.ProjectResponseDTO;
import com.pm.taskapp.task.dto.IssueResponseDTO;
import com.pm.taskapp.task.service.IssueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
}
