package com.pm.taskapp.comments.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDTO {

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;

}
