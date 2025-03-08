package com.eugene.review_service.dto;

public record ReviewDto(
        String comment,
        int rate,
        String userId,
        String bookId
) {
}
