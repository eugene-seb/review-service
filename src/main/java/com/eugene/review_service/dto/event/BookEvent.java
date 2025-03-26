package com.eugene.review_service.dto.event;

import java.util.Set;

public record BookEvent(String eventType, Set<Long> reviewsIds) {
}
