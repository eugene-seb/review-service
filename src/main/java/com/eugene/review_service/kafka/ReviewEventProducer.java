package com.eugene.review_service.kafka;

import com.eugene.review_service.dto.event.ReviewEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReviewEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReviewEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReviewsCreatedEvent(String username, String isbn, Set<Long> reviewsIds) {
        kafkaTemplate.send("review.events",
                new ReviewEvent("REVIEWS_CREATED", username, isbn, reviewsIds));
    }

    public void sendReviewsDeletedEvent(Set<Long> reviewsIds) {
        kafkaTemplate.send("review.events", new ReviewEvent("REVIEWS_DELETED", "", "", reviewsIds));
    }
}
