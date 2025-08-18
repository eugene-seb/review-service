package com.eugene.review_service.kafka;

import com.fasterxml.jackson.annotation.JsonValue;

public enum KafkaEventType {
    USER_DELETED("USER_DELETED"), BOOK_DELETED("BOOK_DELETED"), REVIEWS_CREATED(
            "REVIEWS_CREATED"), REVIEWS_DELETED("REVIEWS_DELETED");

    private final String value;

    KafkaEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
