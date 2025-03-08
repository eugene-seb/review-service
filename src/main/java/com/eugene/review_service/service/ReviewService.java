package com.eugene.review_service.service;

import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;

    public ReviewService(ReviewRepository reviewRepository, UserFeign userFeign, BookFeign bookFeign) {
        this.reviewRepository = reviewRepository;
        this.userFeign = userFeign;
        this.bookFeign = bookFeign;
    }

    public ResponseEntity<Review> createReview(ReviewDto reviewDto) {
        Boolean userExists = userFeign
                .isUserExist(reviewDto.userId())
                .getBody();
        Boolean bookExists = bookFeign
                .isBookExist(reviewDto.bookId())
                .getBody();
        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            Review review = new Review(reviewDto.rate(), reviewDto.comment(), reviewDto.userId(), reviewDto.bookId());
            return ResponseEntity.ok(reviewRepository.save(review));
        } else {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    public ResponseEntity<List<Review>> getAllReview() {
        List<Review> reviews = reviewRepository.findAll();
        return ResponseEntity.ok(reviews);
    }

    public ResponseEntity<Review> getReviewById(Long idReview) {
        Review review = reviewRepository
                .findById(idReview)
                .orElse(null);

        if (review == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity
                    .ok(review);
        }
    }
}
