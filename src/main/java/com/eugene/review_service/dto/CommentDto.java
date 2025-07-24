package com.eugene.review_service.dto;

import com.eugene.review_service.model.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is used for the creation of a Comment.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto
{
    @NotBlank(message = "The content of a comment can't be blank.")
    @Size(min = 2, max = 500, message = "The content must be from 2 to 500 characters.")
    private String content;

    @NotBlank(message = "The user ID is required.")
    private String userId;

    @NotBlank(message = "The book ID is required.")
    private String bookId;

    public Comment toComment() {
        return new Comment(this.content, this.userId, this.bookId);
    }
}