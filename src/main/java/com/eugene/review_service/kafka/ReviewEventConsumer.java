package com.eugene.review_service.kafka;

import com.eugene.review_service.dto.event.BookEvent;
import com.eugene.review_service.dto.event.UserEvent;
import com.eugene.review_service.repository.ReviewRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ReviewEventConsumer {

    private final ReviewRepository reviewRepository;

    public ReviewEventConsumer(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @KafkaListener(topics = "user.events", groupId = "review-service-group")
    public void handleUserDeletedEvent(UserEvent event) {
        if ("USER_DELETED".equals(event.eventType())) {
            deleteReviewsByIds(event.reviewsIds());
        }
    }

    @KafkaListener(topics = "book.events", groupId = "review-service-group")
    public void handleBookDeletedEvent(BookEvent event) {
        if ("BOOK_DELETED".equals(event.eventType())) {
            deleteReviewsByIds(event.reviewsIds());
        }
    }

    private void deleteReviewsByIds(Set<Long> reviewsIds) {
        reviewRepository.deleteAllById(reviewsIds);
    }
}
