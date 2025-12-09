package com.pm.taskapp.task.controller;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.project.dto.response.ProjectResponseDTO;
import com.pm.taskapp.task.dto.IssueCreateRequestDTO;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @ApiResponse(
                    responseCode = "201",
                    description = "Issue created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<IssueResponseDTO> createIssue(@Valid @RequestBody IssueCreateRequestDTO request, @PathVariable @Parameter(description = "Project ID") UUID projectId, @CurrentUser UserPrincipal currentUser) {
        log.info("Creating issue '{}' of project '{}' by user '{}'",request.getTitle(), projectId, currentUser.getId());

        IssueResponseDTO issue = issueService.createIssue(projectId, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }
}
