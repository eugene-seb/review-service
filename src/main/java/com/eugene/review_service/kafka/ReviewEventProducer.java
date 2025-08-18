package com.eugene.review_service.kafka;

import com.eugene.review_service.dto.event.ReviewDtoEvent;
import com.eugene.review_service.exception.JsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReviewEventProducer
{
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ReviewEventProducer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendReviewsCreatedEvent(
            String userId,
            String isbn,
            Set<Long> reviewsIds
    ) {
        try {
            String json = this.objectMapper.writeValueAsString(new ReviewDtoEvent(KafkaEventType.REVIEWS_CREATED,
                                                                                  userId,
                                                                                  isbn,
                                                                                  reviewsIds));
            this.kafkaTemplate.send("review.events",
                                    json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize the list of IDs",
                                    e.getCause());
        }
    }
    
    public void sendReviewsDeletedEvent(Set<Long> reviewsIds) {
        try {
            String json = this.objectMapper.writeValueAsString(new ReviewDtoEvent(KafkaEventType.REVIEWS_DELETED,
                                                                                  "",
                                                                                  "",
                                                                                  reviewsIds));
            this.kafkaTemplate.send("review.events",
                                    json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize the list of IDs",
                                    e.getCause());
        }
    }
}
