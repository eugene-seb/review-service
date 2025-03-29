package com.eugene.review_service.service;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.repository.ReviewRepository;
import com.eugene.review_service.repository.specification.ReviewSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
@Profile("test")
public class ReviewServiceMock {
    private final ReviewRepository reviewRepository;

    public ReviewServiceMock(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public ResponseEntity<Review> createReview(ReviewDto reviewDto) throws URISyntaxException {

        if (reviewDto != null) {
            Review review = new Review(reviewDto.rating(), reviewDto.comment(), reviewDto.userId(),
                    reviewDto.bookId());
            Review reviewCreated = reviewRepository.save(review);

            return ResponseEntity
                    .created(new URI("/review?idReview=" + reviewCreated.getId()))
                    .body(reviewCreated);
        } else {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @Transactional
    public ResponseEntity<List<Review>> getReviewsByBook(String bookId) {
        Specification<Review> reviewSpec = ReviewSpecification.findReviewsByBook(bookId);
        List<Review> reviews = reviewRepository.findAll(reviewSpec);
        return ResponseEntity.ok(reviews);
    }

    @Transactional
    public ResponseEntity<Review> getReviewById(Long idReview) {
        Review review = reviewRepository
                .findById(idReview)
                .orElse(null);

        if (review == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(review);
        }
    }

    @Transactional
    public ResponseEntity<Review> updateRating(RatingDto ratingDto) {
        Specification<Review> reviewSpec = ReviewSpecification.findReviewByUserAndBook(ratingDto);
        Optional<Review> existingRatingOpt = reviewRepository.findOne(reviewSpec);

        if (existingRatingOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
        Review reviewOld = existingRatingOpt.get();
        reviewOld.setRating(ratingDto.ratingUpdated());

        Review reviewUpdated = reviewRepository.save(reviewOld);

        return ResponseEntity.ok(reviewUpdated);
    }

    @Transactional
    public void deleteReview(Long idReview) {
        reviewRepository.deleteById(idReview);
        ResponseEntity
                .ok()
                .build();
    }
}
