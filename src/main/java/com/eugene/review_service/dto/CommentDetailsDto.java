package com.eugene.review_service.dto;

/**
 * This class is used to transfer the information about a comment.
 *
 * @param id
 * @param content
 * @param reviewDate
 * @param userId
 * @param bookId
 */
public record CommentDetailsDto(Long id, String content, String reviewDate, String userId,
                                String bookId) {
}
