package com.eugene.review_service.dto;

public record ReviewDto(String comment, int rating, String userId, String bookId) {
}
