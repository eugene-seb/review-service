package com.eugene.review_service.controller;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("review")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("create_review")
    public ResponseEntity<Review> createReview(@RequestBody ReviewDto reviewDto) throws
            URISyntaxException {
        return reviewService.createReview(reviewDto);
    }

    @GetMapping("reviews/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByBook(@PathVariable String bookId) {
        return reviewService.getReviewsByBook(bookId);
    }

    @GetMapping
    public ResponseEntity<Review> getReviewById(@RequestParam Long idReview) {
        return reviewService.getReviewById(idReview);
    }

    @PutMapping("/update/rating")
    public ResponseEntity<Review> updateRating(@RequestBody RatingDto ratingDto) {
        return reviewService.updateRating(ratingDto);
    }

    @DeleteMapping("delete/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId);
    }
}
