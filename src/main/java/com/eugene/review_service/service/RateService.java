package com.eugene.review_service.service;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Rate;
import com.eugene.review_service.repository.RateRepository;
import com.eugene.review_service.repository.specification.RateSpecification;
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
public class RateService {
    private final ReviewEventProducer reviewEventProducer;
    private final RateRepository rateRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;

    public RateService(
            ReviewEventProducer reviewEventProducer, RateRepository rateRepository,
            UserFeign userFeign, BookFeign bookFeign) {
        this.reviewEventProducer = reviewEventProducer;
        this.rateRepository = rateRepository;
        this.userFeign = userFeign;
        this.bookFeign = bookFeign;
    }

    @Transactional
    public ResponseEntity<RateDetailsDto> createOrUpdateRate(RateDto rateDto) throws
            URISyntaxException {
        Boolean userExists = this.userFeign
                .isUserExist(rateDto.userId())
                .getBody();
        Boolean bookExists = this.bookFeign
                .isBookExist(rateDto.bookId())
                .getBody();

        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            Specification<Rate> rateSpec = RateSpecification.findRateByUserAndBook(rateDto.userId(),
                    rateDto.bookId());
            Rate rate = this.rateRepository
                    .findOne(rateSpec)
                    .orElse(new Rate());

            rate.setScore(rateDto.score());
            rate.setUserId(rateDto.userId());
            rate.setBookId(rateDto.bookId());

            RateDetailsDto rateSaved = this.rateRepository
                    .save(rate)
                    .toRateDetailsDto();
            try {
                this.reviewEventProducer.sendReviewsCreatedEvent(rateDto.userId(), rateDto.bookId(),
                        Set.of(rateSaved.id()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            return ResponseEntity
                    .created(new URI("/rate?idRate=" + rateSaved.id()))
                    .body(rateSaved);
        } else {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @Transactional
    public ResponseEntity<List<RateDetailsDto>> getRatesByBook(String bookId) {
        Specification<Rate> rateSpec = RateSpecification.findRatesByBook(bookId);
        List<RateDetailsDto> reviews = this.rateRepository
                .findAll(rateSpec)
                .stream()
                .map(Rate::toRateDetailsDto)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @Transactional
    public ResponseEntity<RateDetailsDto> getRateById(Long idRate) {
        Rate rate = rateRepository
                .findById(idRate)
                .orElse(null);

        if (rate == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(rate.toRateDetailsDto());
        }
    }

    @Transactional
    public ResponseEntity<Void> deleteRate(Long idRate) {
        rateRepository.deleteById(idRate);
        try {
            reviewEventProducer.sendReviewsDeletedEvent(Set.of(idRate));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return ResponseEntity
                .ok()
                .build();
    }
}
