package com.eugene.review_service.service;

import com.eugene.review_service.dto.RatingDto;
import com.eugene.review_service.dto.ReviewDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Review;
import com.eugene.review_service.repository.ReviewRepository;
import com.eugene.review_service.repository.specification.ReviewSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<Review> createReview(ReviewDto reviewDto) throws URISyntaxException {
        Boolean userExists = userFeign
                .isUserExist(reviewDto.userId())
                .getBody();
        Boolean bookExists = bookFeign
                .isBookExist(reviewDto.bookId())
                .getBody();

        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            Review review = new Review(reviewDto.rating(), reviewDto.comment(), reviewDto.userId(),
                    reviewDto.bookId());
            Review reviewCreated = reviewRepository.save(review);

            reviewEventProducer.sendReviewsCreatedEvent(reviewDto.userId(), reviewDto.bookId(),
                    Set.of(reviewCreated.getId()));

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
    public ResponseEntity<Void> deleteReview(Long idReview) {
        reviewRepository.deleteById(idReview);
        reviewEventProducer.sendReviewsDeletedEvent(Set.of(idReview));
        return ResponseEntity
                .ok()
                .build();
    }
}
