package com.eugene.review_service.service;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Comment;
import com.eugene.review_service.repository.CommentRepository;
import com.eugene.review_service.repository.specification.CommentSpecification;
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
public class CommentService {
    private final ReviewEventProducer reviewEventProducer;
    private final CommentRepository commentRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;

    public CommentService(
            ReviewEventProducer reviewEventProducer, CommentRepository commentRepository,
            UserFeign userFeign, BookFeign bookFeign) {
        this.reviewEventProducer = reviewEventProducer;
        this.commentRepository = commentRepository;
        this.userFeign = userFeign;
        this.bookFeign = bookFeign;
    }

    @Transactional
    public ResponseEntity<CommentDetailsDto> createComment(CommentDto commentDto) throws
            URISyntaxException {
        Boolean userExists = userFeign
                .isUserExist(commentDto.userId())
                .getBody();
        Boolean bookExists = bookFeign
                .isBookExist(commentDto.bookId())
                .getBody();

        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            Comment comment = commentDto.toComment();
            CommentDetailsDto commentCreated = commentRepository
                    .save(comment)
                    .toCommentDetailsDto();
            try {
                reviewEventProducer.sendReviewsCreatedEvent(commentDto.userId(),
                        commentDto.bookId(), Set.of(commentCreated.id()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
            return ResponseEntity
                    .created(new URI("/comment?idComment=" + commentCreated.id()))
                    .body(commentCreated);
        } else {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @Transactional
    public ResponseEntity<List<CommentDetailsDto>> getCommentsByBook(String bookId) {
        Specification<Comment> commentSpec = CommentSpecification.findCommentsByBook(bookId);
        List<CommentDetailsDto> comments = commentRepository
                .findAll(commentSpec)
                .stream()
                .map(Comment::toCommentDetailsDto)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @Transactional
    public ResponseEntity<CommentDetailsDto> getCommentById(Long idComment) {
        Comment comment = commentRepository
                .findById(idComment)
                .orElse(null);

        if (comment == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(comment.toCommentDetailsDto());
        }
    }

    @Transactional
    public ResponseEntity<CommentDetailsDto> updateComment(CommentDetailsDto commentDetailsDto) {
        Comment comment = commentRepository
                .findById(commentDetailsDto.id())
                .orElse(null);

        if (comment == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            comment.setContent(commentDetailsDto.content());

            CommentDetailsDto reviewUpdated = commentRepository
                    .save(comment)
                    .toCommentDetailsDto();

            return ResponseEntity.ok(reviewUpdated);
        }
    }

    @Transactional
    public ResponseEntity<Void> deleteComment(Long idComment) {
        commentRepository.deleteById(idComment);
        try {
            reviewEventProducer.sendReviewsDeletedEvent(Set.of(idComment));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return ResponseEntity
                .ok()
                .build();
    }
}
