package com.eugene.review_service.model;

import com.eugene.review_service.dto.CommentDetailsDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Comment extends Review {

    @Column(nullable = false, updatable = false)
    private String content;

    public Comment(String content, String userId, String bookId) {
        super(userId, bookId);
        this.content = content;
    }

    public CommentDetailsDto toCommentDetailsDto() {
        return new CommentDetailsDto(this.id, this.content, this.reviewDate.toString(), this.userId,
                this.bookId);
    }

}
