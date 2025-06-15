package com.eugene.review_service.unit.controller;

import com.eugene.review_service.controller.ReviewController;
import com.eugene.review_service.dto.ReviewDetailsDto;
import com.eugene.review_service.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
    void getReviewsByBook() throws Exception {
        List<ReviewDetailsDto> reviews = List.of(
                new ReviewDetailsDto(5L, 4, "String comment", LocalDateTime.now(), "String userId",
                        "String bookId"));

        given(reviewService.getReviewsByBook("String bookId")).willReturn(
                ResponseEntity.ok(reviews));

        mockMvc
                .perform(get("/review/reviews/book/{bookId}", "String bookId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(reviews.size()));

        verify(reviewService).getReviewsByBook("String bookId");
    }
}
