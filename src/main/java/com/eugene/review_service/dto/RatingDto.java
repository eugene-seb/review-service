package com.eugene.review_service.dto;

public record RatingDto(String userId, String bookId, int ratingUpdated) {
}
