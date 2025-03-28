package com.eugene.review_service.kafka;

import com.eugene.review_service.dto.event.BookDtoEvent;
import com.eugene.review_service.dto.event.UserDtoEvent;
import com.eugene.review_service.repository.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReviewEventConsumer {

    private final Logger log = LoggerFactory.getLogger(ReviewEventConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReviewRepository reviewRepository;

    public ReviewEventConsumer(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @KafkaListener(topics = "user.events", groupId = "review-service-group")
    public void handleUserDeletedEvent(String json) throws JsonProcessingException {
        UserDtoEvent userDtoEvent = objectMapper.readValue(json, UserDtoEvent.class);
        if (userDtoEvent.getEventType() == KafkaEventType.USER_DELETED) {
            deleteReviewsByIds(userDtoEvent.getReviewsIds());
        }
    }

    @KafkaListener(topics = "book.events", groupId = "review-service-group")
    public void handleBookDeletedEvent(String json) throws JsonProcessingException {
        BookDtoEvent bookDtoEvent = objectMapper.readValue(json, BookDtoEvent.class);
        if (bookDtoEvent.getEventType() == KafkaEventType.BOOK_DELETED) {
            deleteReviewsByIds(bookDtoEvent.getReviewsIds());
        }
    }

    private void deleteReviewsByIds(Set<Long> reviewsIds) {
        reviewRepository.deleteAllById(reviewsIds);
        log.info("Reviews deleted");
    }
}
