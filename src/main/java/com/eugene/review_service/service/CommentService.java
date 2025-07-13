package com.eugene.review_service.service;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.exception.NotFoundException;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.eugene.review_service.kafka.ReviewEventProducer;
import com.eugene.review_service.model.Comment;
import com.eugene.review_service.repository.CommentRepository;
import com.eugene.review_service.repository.specification.CommentSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static String getCommentNotFoundMessage(long idComment) {
        return "Comment '" + idComment + "' not found.";
    }

    @Transactional
    public CommentDetailsDto createComment(CommentDto commentDto) {
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

            reviewEventProducer.sendReviewsCreatedEvent(commentDto.userId(), commentDto.bookId(),
                    Set.of(commentCreated.id()));
            return commentCreated;
        } else {
            throw new NotFoundException("The user or book do not exist.", null);
        }
    }

    @Transactional
    public List<CommentDetailsDto> getCommentsByBook(String bookId) {
        Specification<Comment> commentSpec = CommentSpecification.findCommentsByBook(bookId);
        return commentRepository
                .findAll(commentSpec)
                .stream()
                .map(Comment::toCommentDetailsDto)
                .toList();
    }

    @Transactional
    public CommentDetailsDto getCommentById(Long idComment) {
        return commentRepository
                .findById(idComment)
                .map(Comment::toCommentDetailsDto)
                .orElseThrow(
                        () -> new NotFoundException(getCommentNotFoundMessage(idComment), null));
    }

    @Transactional
    public CommentDetailsDto updateComment(CommentDetailsDto commentDetailsDto) {
        Comment comment = commentRepository
                .findById(commentDetailsDto.id())
                .orElseThrow(() -> new NotFoundException(
                        getCommentNotFoundMessage(commentDetailsDto.id()), null));

        comment.setContent(commentDetailsDto.content());

        return commentRepository
                .save(comment)
                .toCommentDetailsDto();
    }

    @Transactional
    public void deleteComment(Long idComment) {
        commentRepository.deleteById(idComment);
        reviewEventProducer.sendReviewsDeletedEvent(Set.of(idComment));
    }
}
