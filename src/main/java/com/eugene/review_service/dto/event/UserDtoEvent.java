package com.eugene.review_service.dto.event;

import com.eugene.review_service.kafka.KafkaEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserDtoEvent extends BaseDtoEvent {

    private Set<Long> reviewsIds;

    public UserDtoEvent(KafkaEventType eventType, Set<Long> reviewsIds) {
        super(eventType);
        this.reviewsIds = reviewsIds;
    }
}
