package com.eugene.review_service.controller;

import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("review")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("create_review")
    public ResponseEntity<Review> createReview(@RequestBody ReviewDto reviewDto) {
        return reviewService.createReview(reviewDto);
    }

    @GetMapping("all_reviews")
    public ResponseEntity<List<Review>> getAllReview() {
        return reviewService.getAllReview();
    }

    @GetMapping
    public ResponseEntity<Review> getReviewById(@RequestParam Long idReview) {
        return reviewService.getReviewById(idReview);
    }
}
