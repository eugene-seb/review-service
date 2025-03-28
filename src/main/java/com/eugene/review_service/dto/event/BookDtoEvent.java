package com.eugene.review_service.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class BookDtoEvent extends BaseDtoEvent {

    private Set<Long> reviewsIds;

    public BookDtoEvent(String eventType, Set<Long> reviewsIds) {
        super(eventType);
        this.reviewsIds = reviewsIds;
    }
}
