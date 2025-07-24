package com.eugene.review_service.service;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.exception.NotFoundException;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Rate;
import com.eugene.review_service.repository.RateRepository;
import com.eugene.review_service.repository.specification.RateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class RateService
{
    private final ReviewEventProducer reviewEventProducer;
    private final RateRepository rateRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;

    public RateService(
            ReviewEventProducer reviewEventProducer,
            RateRepository rateRepository,
            UserFeign userFeign,
            BookFeign bookFeign
    ) {
        this.reviewEventProducer = reviewEventProducer;
        this.rateRepository = rateRepository;
        this.userFeign = userFeign;
        this.bookFeign = bookFeign;
    }

    @Transactional
    public RateDetailsDto createOrUpdateRate(RateDto rateDto) {
        Boolean userExists = this.userFeign.isUserExist(rateDto.getUserId())
                                           .getBody();
        Boolean bookExists = this.bookFeign.isBookExist(rateDto.getBookId())
                                           .getBody();

        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {

            Specification<Rate> rateSpec = RateSpecification.findRateByUserAndBook(
                    rateDto.getUserId(), rateDto.getBookId());

            Rate rate = this.rateRepository.findOne(rateSpec)
                                           .orElse(new Rate());
            rate.setScore(rateDto.getScore());
            rate.setUserId(rateDto.getUserId());
            rate.setBookId(rateDto.getBookId());

            RateDetailsDto rateSaved = this.rateRepository.save(rate)
                                                          .toRateDetailsDto();

            this.reviewEventProducer.sendReviewsCreatedEvent(rateDto.getUserId(),
                                                             rateDto.getBookId(),
                                                             Set.of(rateSaved.getId()));
            return rateSaved;
        } else {
            throw new NotFoundException("The user or book do not exist.", null);
        }
    }

    @Transactional
    public List<RateDetailsDto> getRatesByBook(String bookId) {
        Specification<Rate> rateSpec = RateSpecification.findRatesByBook(bookId);
        return this.rateRepository.findAll(rateSpec)
                                  .stream()
                                  .map(Rate::toRateDetailsDto)
                                  .toList();
    }

    @Transactional
    public RateDetailsDto getRateById(Long idRate) {
        return this.rateRepository.findById(idRate)
                                  .map(Rate::toRateDetailsDto)
                                  .orElseThrow(() -> new NotFoundException(
                                          "Rate '" + idRate + "' not found.", null));
    }

    @Transactional
    public void deleteRate(Long idRate) {
        this.rateRepository.deleteById(idRate);
        this.reviewEventProducer.sendReviewsDeletedEvent(Set.of(idRate));
    }
}
