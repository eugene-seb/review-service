package com.eugene.review_service.model;

import com.eugene.review_service.dto.ReviewDetailsDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime reviewDate;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private String bookId;

    public Review(int rate, String comment, String userId, String bookId) {
        this.rating = rate;
        this.comment = comment;
        this.userId = userId;
        this.bookId = bookId;
    }

    @PrePersist
    protected void onCreate() {
        this.reviewDate = LocalDateTime.now();
    }

    public ReviewDetailsDto toReviewDetailsDto() {
        return new ReviewDetailsDto(this.id, this.rating, this.comment, this.reviewDate,
                this.userId, this.bookId);
    }

}
