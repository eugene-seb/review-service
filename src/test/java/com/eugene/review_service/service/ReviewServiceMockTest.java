package com.eugene.review_service.service;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    /// It will be used in each function of service that call Kafka producer/consumer
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

    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    void getReviewsByBook() throws Exception {

        mockMvc
                .perform(get("/review/reviews/book/{bookId}", this.reviewDto.bookId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(3)
    void getReviewById() throws Exception {

        mockMvc
                .perform(get("/review?idReview={idReview}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value(this.reviewDto.comment()));
    }

    @Test
    @Order(4)
    void updateRating() throws Exception {

        mockMvc
                .perform(put("/review/update/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ratingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(ratingDto.ratingUpdated()));
    }

    @Test
    @Order(5)
    void deleteReview() throws Exception {

        mockMvc
                .perform(delete("/review/delete/{idReview}", 1))
                .andExpect(status().isOk());

        mockMvc
                .perform(get("/review?idReview={idReview}", 1))
                .andExpect(status().isNotFound());
    }
}
