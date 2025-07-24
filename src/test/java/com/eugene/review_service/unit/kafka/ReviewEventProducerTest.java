package com.eugene.review_service.unit.kafka;

import com.eugene.review_service.dto.event.ReviewDtoEvent;
import com.eugene.review_service.kafka.KafkaEventType;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ReviewEventProducerTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private ReviewEventProducer reviewEventProducer;

    @Test
    void sendReviewsCreatedEvent() throws JsonProcessingException {

        Set<Long> reviewsIds = Set.of(1L, 2L);

        this.reviewEventProducer.sendReviewsCreatedEvent("username", "isbn", reviewsIds);

        String json = this.objectMapper.writeValueAsString(
                new ReviewDtoEvent(KafkaEventType.REVIEWS_CREATED, "username", "isbn", reviewsIds));

        verify(this.kafkaTemplate, times(1)).send("review.events", json);
    }

    @Test
    void sendReviewsDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewsIds = Set.of(1L, 2L);

        this.reviewEventProducer.sendReviewsDeletedEvent(reviewsIds);

        String json = this.objectMapper.writeValueAsString(
                new ReviewDtoEvent(KafkaEventType.REVIEWS_DELETED, "", "", reviewsIds));

        verify(this.kafkaTemplate, times(1)).send("review.events", json);
    }
}
