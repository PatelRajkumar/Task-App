package com.pm.taskapp.comments.mapper;

import org.springframework.stereotype.Component;

import com.pm.taskapp.comments.dto.CommentResponseDTO;
import com.pm.taskapp.comments.dto.CommentSummaryDTO;
import com.pm.taskapp.comments.entity.Comment;
import com.pm.taskapp.project.mapper.ProjectMapper;
import com.pm.taskapp.task.mapper.IssueMapper;

@Component
public class CommentMapper {

    private final IssueMapper issueMapper;
    private final ProjectMapper projectMapper;

    public CommentMapper(IssueMapper issueMapper, ProjectMapper projectMapper) {
        this.issueMapper = issueMapper;
        this.projectMapper = projectMapper;
    }

    public CommentResponseDTO toResponseDTO(Comment comment) {
        if (comment == null)
            return null;

        CommentResponseDTO.CommentResponseDTOBuilder builder = CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .issue(issueMapper.toSummaryDTO(comment.getIssue()))
                .author(projectMapper.toUserSummaryDTO(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt());
        return builder.build();
    }

    public CommentSummaryDTO toSummaryDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentSummaryDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(projectMapper.toUserSummaryDTO(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt()).build();
    }

}
