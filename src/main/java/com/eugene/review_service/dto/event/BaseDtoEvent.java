package com.eugene.review_service.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BaseDtoEvent {
    private String eventType;

    public BaseDtoEvent(String eventType) {
        this.eventType = eventType;
    }
}
