package com.eugene.review_service.dto;

import com.eugene.review_service.model.Rate;

/**
 * This class is used for the creation of a rate.
 *
 * @param score  The rate of the review
 * @param userId The ID of the user who created the review
 * @param bookId The ID of the book being reviewed
 */
public record RateDto(int score, String userId, String bookId) {
    public Rate toRate() {
        return new Rate(this.score, this.userId, this.bookId);
    }
}
