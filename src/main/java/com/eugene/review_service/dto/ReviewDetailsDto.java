package com.eugene.review_service.dto;

import java.time.LocalDateTime;

/**
 * This class is used to transfer the information about a review.
 *
 * @param id
 * @param rating
 * @param comment
 * @param reviewDate
 * @param userId
 * @param bookId
 */
public record ReviewDetailsDto(Long id, int rating, String comment, LocalDateTime reviewDate,
                               String userId, String bookId) {
}
