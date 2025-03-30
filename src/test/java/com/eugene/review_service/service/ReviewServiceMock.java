package com.eugene.review_service.service;

import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.repository.ReviewRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;

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

}
