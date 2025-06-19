package com.eugene.review_service.dto;

/**
 * This class is used to transfer the information about a rate.
 *
 * @param id
 * @param score
 * @param reviewDate
 * @param userId
 * @param bookId
 */
public record RateDetailsDto(Long id, int score, String reviewDate, String userId, String bookId) {
}
