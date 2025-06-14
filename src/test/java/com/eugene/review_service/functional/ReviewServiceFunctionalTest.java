package com.eugene.review_service.functional;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
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
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReviewServiceFunctionalTest {

    private final ReviewDto reviewDto;
    private final RatingDto ratingDto;

    /// I don't want the context to load kafka for this test, so I'm mocking his initialization
    /// It will replace all the KafkaTemplate instances.
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;
    @MockitoBean
    private UserFeign userFeign;
    @MockitoBean
    private BookFeign bookFeign;

    @Autowired
    private MockMvc mockMvc;

    public ReviewServiceFunctionalTest() {
        this.reviewDto = new ReviewDto("String comment", 4, "user1", "book4");
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

        when(userFeign.isUserExist(anyString())).thenReturn(ResponseEntity.ok(true));
        when(bookFeign.isBookExist(anyString())).thenReturn(ResponseEntity.ok(true));

        mockMvc
                .perform(post("/review/create_review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(this.reviewDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(this.reviewDto.userId()));

        verify(userFeign).isUserExist(anyString());
        verify(bookFeign).isBookExist(anyString());
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
