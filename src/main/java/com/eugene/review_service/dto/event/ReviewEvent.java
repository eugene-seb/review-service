package com.eugene.review_service.dto.event;

import java.util.Set;

public record ReviewEvent(String eventType, String username, String isbn, Set<Long> reviewsIds) {
}
