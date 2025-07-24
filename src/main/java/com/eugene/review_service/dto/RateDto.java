package com.eugene.review_service.dto;

import com.eugene.review_service.model.Rate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is used for the creation of a rate.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RateDto
{
    @NotNull(message = "The score can't be null.")
    @PositiveOrZero(message = "The score should be greater or equal to 0.")
    private int score;

    @NotBlank(message = "The user ID is required.")
    private String userId;

    @NotBlank(message = "The book ID is required.")
    private String bookId;

    public Rate toRate() {
        return new Rate(this.score, this.userId, this.bookId);
    }
}