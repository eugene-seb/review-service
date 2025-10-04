package com.eugene.review_service.service;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.exception.NotFoundException;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Rate;
import com.eugene.review_service.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RateService
{
    private final ReviewEventProducer reviewEventProducer;
    private final RateRepository rateRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;
    
    @Transactional
    public RateDetailsDto createOrUpdateRate(RateDto rateDto) {
        Boolean userExists = this.userFeign
                .isUserExist(rateDto.getUserId())
                .getBody();
        Boolean bookExists = this.bookFeign
                .isBookExist(rateDto.getBookId())
                .getBody();
        
        if (rateDto.getScore() > 5 || rateDto.getScore() < 0) {
            throw new IllegalArgumentException("The score should be 0 < score < 6");
        } else if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            
            Rate rate = this.rateRepository
                    .findByUserIdAndBookId(rateDto.getUserId(),
                                           rateDto.getBookId())
                    .orElse(new Rate(rateDto.getScore(),
                                     rateDto.getUserId(),
                                     rateDto.getBookId()));
            // Update score if rate already exists
            rate.setScore(rateDto.getScore());
            
            RateDetailsDto rateSaved = this.rateRepository
                    .save(rate)
                    .toRateDetailsDto();
            
            this.reviewEventProducer.sendReviewsCreatedEvent(rateDto.getUserId(),
                                                             rateDto.getBookId(),
                                                             Set.of(rateSaved.getId()));
            return rateSaved;
        } else {
            throw new NotFoundException("The user or book do not exist.",
                                        null);
        }
    }
    
    @Transactional(readOnly = true)
    public List<RateDetailsDto> getRatesByBook(String bookId) {
        return this.rateRepository
                .findAllByBookId(bookId)
                .stream()
                .map(Rate::toRateDetailsDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public RateDetailsDto getRateById(Long idRate) {
        return this.rateRepository
                .findById(idRate)
                .map(Rate::toRateDetailsDto)
                .orElseThrow(() -> new NotFoundException("Rate '" + idRate + "' not found.",
                                                         null));
    }
    
    @Transactional
    public void deleteRate(Long idRate) {
        Rate rate = this.rateRepository
                .findById(idRate)
                .orElseThrow(() -> new NotFoundException("Rate '" + idRate + "' not found.",
                                                         null));
        this.rateRepository.delete(rate);
        this.reviewEventProducer.sendReviewsDeletedEvent(Set.of(rate.getId()));
    }
}
