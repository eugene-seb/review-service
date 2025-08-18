package com.eugene.review_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * This class is used to transfer the information about a comment.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDetailsDto
{
    @NotNull(message = "The ID is required.")
    @Positive(message = "The ID must be a positive value.")
    private Long id;

    @NotBlank(message = "The content of a comment can't be blank.")
    @Size(min = 2, max = 500, message = "The content must be from 2 to 500 characters.")
    private String content;

    @Past(message = "The date should be in the past.")
    private LocalDateTime reviewDate;

    @NotBlank(message = "The user ID is required.")
    private String userId;

    @NotBlank(message = "The book ID is required.")
    private String bookId;
}