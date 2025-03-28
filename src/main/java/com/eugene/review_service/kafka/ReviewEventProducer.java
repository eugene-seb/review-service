package com.eugene.review_service.kafka;

import com.eugene.review_service.dto.event.ReviewDtoEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReviewEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReviewsCreatedEvent(String username, String isbn, Set<Long> reviewsIds) throws
            JsonProcessingException {
        String json = objectMapper.writeValueAsString(
                new ReviewDtoEvent("REVIEWS_CREATED", username, isbn, reviewsIds));
        kafkaTemplate.send("review.events", json);
    }

    public void sendReviewsDeletedEvent(Set<Long> reviewsIds) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(
                new ReviewDtoEvent("REVIEWS_DELETED", "", "", reviewsIds));
        kafkaTemplate.send("review.events", json);
    }
}
