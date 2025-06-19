package com.eugene.review_service.dto;

import com.eugene.review_service.model.Comment;

/**
 * This class is used for the creation of a Comment.
 *
 * @param content The comment of the review
 * @param userId  The ID of the user who created the review
 * @param bookId  The ID of the book being reviewed
 */
public record CommentDto(String content, String userId, String bookId) {
    public Comment toComment() {
        return new Comment(this.content, this.userId, this.bookId);
    }
}
