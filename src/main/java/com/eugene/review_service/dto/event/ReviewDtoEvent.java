package com.eugene.review_service.dto.event;

import com.eugene.review_service.kafka.KafkaEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
public class ReviewDtoEvent extends BaseDtoEvent {

    private String username;
    private String isbn;
    private Set<Long> reviewsIds;

    public ReviewDtoEvent(
            KafkaEventType eventType, String username, String isbn, Set<Long> reviewsIds) {
        super(eventType);
        this.username = username;
        this.isbn = isbn;
        this.reviewsIds = reviewsIds;
    }
}
