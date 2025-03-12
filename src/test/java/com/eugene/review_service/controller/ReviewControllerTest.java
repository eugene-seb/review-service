package com.eugene.review_service.controller;

import com.eugene.review_service.model.Review;
import com.eugene.review_service.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReviewController.class)
@ActiveProfiles("test")
class ReviewControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReviewService reviewService;

    @Test
    void getAllReview() throws Exception {
        List<Review> reviews = List.of(new Review(4, "String comment", "String userId", "String bookId"));
        reviews
                .getFirst()
                .setId(5L);

        given(reviewService
                .getAllReview())
                .willReturn(ResponseEntity.ok(reviews));

        mockMvc
                .perform(get("/review/all_reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(reviews.size()));

        verify(reviewService).getAllReview();
    }
}
