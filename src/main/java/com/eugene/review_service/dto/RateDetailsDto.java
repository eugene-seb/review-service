package com.eugene.review_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * This class is used to transfer the information about a rate.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RateDetailsDto
{
    @NotNull(message = "The ID is required.")
    @Positive(message = "The ID must be a positive value.")
    private Long id;

    @NotNull(message = "The score can't be null.")
    @PositiveOrZero(message = "The score should be greater or equal to 0.")
    private int score;

    @Past(message = "The date should be in the past.")
    private LocalDateTime reviewDate;

    @NotBlank(message = "The user ID is required.")
    private String userId;

    @NotBlank(message = "The book ID is required.")
    private String bookId;
}