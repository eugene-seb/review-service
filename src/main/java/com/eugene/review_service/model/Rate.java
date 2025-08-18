package com.eugene.review_service.model;

import com.eugene.review_service.dto.RateDetailsDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Rate
        extends Review
{
    @Column(nullable = false, updatable = false)
    private int score;
    
    public Rate(
            int score,
            String userId,
            String bookId
    ) {
        super(userId,
              bookId);
        this.score = score;
    }
    
    public RateDetailsDto toRateDetailsDto() {
        return new RateDetailsDto(this.id,
                                  this.score,
                                  this.reviewDate,
                                  this.userId,
                                  this.bookId);
    }
}
