package com.eugene.review_service.service;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDetailsDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.repository.ReviewRepository;
import com.eugene.review_service.repository.specification.ReviewSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@Service
public class ReviewService {
    private final ReviewEventProducer reviewEventProducer;
    private final ReviewRepository reviewRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;

    public ReviewService(
            ReviewEventProducer reviewEventProducer, ReviewRepository reviewRepository,
            UserFeign userFeign, BookFeign bookFeign) {
        this.reviewEventProducer = reviewEventProducer;
        this.reviewRepository = reviewRepository;
        this.userFeign = userFeign;
        this.bookFeign = bookFeign;
    }

    @Transactional
    public ResponseEntity<ReviewDetailsDto> createReview(ReviewDto reviewDto) throws
            URISyntaxException {
        Boolean userExists = userFeign
                .isUserExist(reviewDto.userId())
                .getBody();
        Boolean bookExists = bookFeign
                .isBookExist(reviewDto.bookId())
                .getBody();

        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            Review review = reviewDto.toReview();
            ReviewDetailsDto reviewCreated = reviewRepository
                    .save(review)
                    .toReviewDetailsDto();
            try {
                reviewEventProducer.sendReviewsCreatedEvent(reviewDto.userId(), reviewDto.bookId(),
                        Set.of(reviewCreated.id()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            return ResponseEntity
                    .created(new URI("/review?idReview=" + reviewCreated.id()))
                    .body(reviewCreated);
        } else {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @Transactional
    public ResponseEntity<List<ReviewDetailsDto>> getReviewsByBook(String bookId) {
        Specification<Review> reviewSpec = ReviewSpecification.findReviewsByBook(bookId);
        List<ReviewDetailsDto> reviews = reviewRepository
                .findAll(reviewSpec)
                .stream()
                .map(Review::toReviewDetailsDto)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @Transactional
    public ResponseEntity<ReviewDetailsDto> getReviewById(Long idReview) {
        Review review = reviewRepository
                .findById(idReview)
                .orElse(null);

        if (review == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(review.toReviewDetailsDto());
        }
    }

    @Transactional
    public ResponseEntity<ReviewDetailsDto> updateRating(RatingDto ratingDto) {
        Specification<Review> reviewSpec = ReviewSpecification.findReviewByUserAndBook(
                ratingDto.userId(), ratingDto.bookId());
        Review review = reviewRepository
                .findOne(reviewSpec)
                .orElse(null);

        if (review == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            review.setRating(ratingDto.ratingUpdated());

            ReviewDetailsDto reviewUpdated = reviewRepository
                    .save(review)
                    .toReviewDetailsDto();

            return ResponseEntity.ok(reviewUpdated);
        }

    }

    @Transactional
    public ResponseEntity<Void> deleteReview(Long idReview) {
        reviewRepository.deleteById(idReview);
        try {
            reviewEventProducer.sendReviewsDeletedEvent(Set.of(idReview));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return ResponseEntity
                .ok()
                .build();
    }
}
