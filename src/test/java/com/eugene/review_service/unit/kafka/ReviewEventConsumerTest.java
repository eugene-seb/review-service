package com.eugene.review_service.unit.kafka;

import com.eugene.review_service.dto.event.BookDtoEvent;
import com.eugene.review_service.dto.event.UserDtoEvent;
import com.eugene.review_service.kafka.KafkaEventType;
import com.eugene.review_service.kafka.ReviewEventConsumer;
import com.eugene.review_service.repository.CommentRepository;
import com.eugene.review_service.repository.RateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ReviewEventConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RateRepository rateRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ReviewEventConsumer reviewEventConsumer;

    @Test
    void handleUserDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewIdsToDelete = Set.of(3L);

        UserDtoEvent userDtoEvent = new UserDtoEvent(KafkaEventType.BOOK_DELETED,
                reviewIdsToDelete);
        String json = objectMapper.writeValueAsString(userDtoEvent);

        doNothing()
                .when(rateRepository)
                .deleteAllById(reviewIdsToDelete);
        doNothing()
                .when(commentRepository)
                .deleteAllById(reviewIdsToDelete);

        reviewEventConsumer.handleBookDeletedEvent(json);

        verify(rateRepository, times(1)).deleteAllById(reviewIdsToDelete);
        verify(commentRepository, times(1)).deleteAllById(reviewIdsToDelete);
    }

    @Test
    void handleBookDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewIdsToDelete = Set.of(3L);

        BookDtoEvent bookDtoEvent = new BookDtoEvent(KafkaEventType.BOOK_DELETED,
                reviewIdsToDelete);
        String json = objectMapper.writeValueAsString(bookDtoEvent);

        doNothing()
                .when(rateRepository)
                .deleteAllById(reviewIdsToDelete);
        doNothing()
                .when(commentRepository)
                .deleteAllById(reviewIdsToDelete);

        reviewEventConsumer.handleBookDeletedEvent(json);

        verify(rateRepository, times(1)).deleteAllById(reviewIdsToDelete);
        verify(commentRepository, times(1)).deleteAllById(reviewIdsToDelete);
    }
}
