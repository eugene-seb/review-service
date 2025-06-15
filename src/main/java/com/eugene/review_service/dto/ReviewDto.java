package com.eugene.review_service.dto;

import com.eugene.review_service.model.Review;

/**
 * This class is used for the creation of a review.
 *
 * @param comment The comment of the review
 * @param rating  The rating of the review
 * @param userId  The ID of the user who created the review
 * @param bookId  The ID of the book being reviewed
 */
public record ReviewDto(int rating, String comment, String userId, String bookId) {
    public Review toReview() {
        return new Review(this.rating, this.comment, this.userId, this.bookId);
    }
}
