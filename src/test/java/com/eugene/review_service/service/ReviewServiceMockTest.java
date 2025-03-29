package com.eugene.review_service.service;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.model.Review;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReviewServiceMockTest {

    private final ReviewDto reviewDto;
    private final RatingDto ratingDto;

    /// I don't want the context to load kafka for this test, so I'm mocking his initialization
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReviewServiceMock reviewServiceMock;

    public ReviewServiceMockTest() {
        this.reviewDto = new ReviewDto("String comment", 4, "review1", "book4");
        this.ratingDto = new RatingDto(this.reviewDto.userId(), this.reviewDto.bookId(), 5);
    }

    @Test
    @Order(1)
    void createReview() throws Exception {
        Review review = this.reviewServiceMock
                .createReview(reviewDto)
                .getBody();
        assertThat(review).isNotNull();
        assertThat(review.getUserId()).isEqualTo(reviewDto.userId());
    }

    @Test
    @Order(2)
    void getReviewsByBook() {

        List<Review> reviews = this.reviewServiceMock
                .getReviewsByBook(this.reviewDto.bookId())
                .getBody();

        assertThat(reviews)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    @Order(3)
    void getReviewById() {
        ResponseEntity<Review> reviewResponseEntity = this.reviewServiceMock.getReviewById(1L);

        assertThat(reviewResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects
                .requireNonNull(reviewResponseEntity.getBody())
                .getComment()).isEqualTo(this.reviewDto.comment());
    }

    @Test
    @Order(4)
    void updateRating() {
        ResponseEntity<Review> reviewResponseEntity = this.reviewServiceMock.updateRating(
                this.ratingDto);
        assertThat(reviewResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects
                .requireNonNull(reviewResponseEntity.getBody())
                .getRating()).isEqualTo(this.ratingDto.ratingUpdated());
    }

    @Test
    @Order(5)
    void deleteReview() throws Exception {

        this.reviewServiceMock.deleteReview(1L);

        mockMvc
                .perform(get("/review?idReview={idReview}", 1))
                .andExpect(status().isNotFound());
    }
}
