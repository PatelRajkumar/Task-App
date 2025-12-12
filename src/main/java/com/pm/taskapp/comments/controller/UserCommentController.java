package com.pm.taskapp.comments.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.taskapp.auth.security.CurrentUser;
import com.pm.taskapp.auth.security.UserPrincipal;
import com.pm.taskapp.comments.dto.CommentSummaryDTO;
import com.pm.taskapp.comments.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Comments", description = "User activity - comments by user")
@SecurityRequirement(name = "bearerAuth")
public class UserCommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "Get user comments", 
        description = "Retrieve all comments by a specific user (paginated).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<CommentSummaryDTO>> getUserComments(
            @PathVariable @Parameter(description = "User ID") UUID userId,
            @PageableDefault(size = 20, sort = "createdAt") @Parameter(description = "Pagination") Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {
        
        log.debug("Getting comments by user: {} requested by: {}", userId, currentUser.getId());
        
        Page<CommentSummaryDTO> comments = commentService.getUserComments(userId, currentUser.getId(), pageable);
        
        return ResponseEntity.ok(comments);
    }
}