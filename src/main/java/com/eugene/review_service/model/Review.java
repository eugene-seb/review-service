package com.eugene.review_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Setter
@Getter
@NoArgsConstructor
public class Review
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false, updatable = false)
    protected LocalDateTime reviewDate;

    @Column(nullable = false, updatable = false)
    protected String userId;

    @Column(nullable = false, updatable = false)
    protected String bookId;

    public Review(
            String userId,
            String bookId
    ) {
        this.userId = userId;
        this.bookId = bookId;
    }

    @PrePersist
    protected void onCreate() {
        this.reviewDate = LocalDateTime.now();
    }
}
