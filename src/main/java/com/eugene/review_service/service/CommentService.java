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
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService
{
    private final ReviewEventProducer reviewEventProducer;
    private final CommentRepository commentRepository;
    private final UserFeign userFeign;
    private final BookFeign bookFeign;
    
    private static String getCommentNotFoundMessage(long idComment) {
        return "Comment '" + idComment + "' not found.";
    }
    
    @Transactional
    public CommentDetailsDto createComment(CommentDto commentDto) {
        Boolean userExists = this.userFeign
                .isUserExist(commentDto.getUserId())
                .getBody();
        Boolean bookExists = this.bookFeign
                .isBookExist(commentDto.getBookId())
                .getBody();
        
        if (Boolean.TRUE.equals(userExists) && Boolean.TRUE.equals(bookExists)) {
            
            Comment comment = commentDto.toComment();
            CommentDetailsDto commentCreated = this.commentRepository
                    .save(comment)
                    .toCommentDetailsDto();
            
            this.reviewEventProducer.sendReviewsCreatedEvent(commentDto.getUserId(),
                                                             commentDto.getBookId(),
                                                             Set.of(commentCreated.getId()));
            return commentCreated;
        } else {
            throw new NotFoundException("The user or book do not exist.",
                                        null);
        }
    }
    
    @Transactional
    public List<CommentDetailsDto> getCommentsByBook(String bookId) {
        Specification<Comment> commentSpec = CommentSpecification.findCommentsByBook(bookId);
        return this.commentRepository
                .findAll(commentSpec)
                .stream()
                .map(Comment::toCommentDetailsDto)
                .toList();
    }
    
    @Transactional
    public CommentDetailsDto getCommentById(Long idComment) {
        return this.commentRepository
                .findById(idComment)
                .map(Comment::toCommentDetailsDto)
                .orElseThrow(() -> new NotFoundException(getCommentNotFoundMessage(idComment),
                                                         null));
    }
    
    @Transactional
    public CommentDetailsDto updateComment(CommentDetailsDto commentDetailsDto) {
        Comment comment = this.commentRepository
                .findById(commentDetailsDto.getId())
                .orElseThrow(() -> new NotFoundException(getCommentNotFoundMessage(commentDetailsDto.getId()),
                                                         null));
        
        comment.setContent(commentDetailsDto.getContent());
        
        return this.commentRepository
                .save(comment)
                .toCommentDetailsDto();
    }
    
    @Transactional
    public void deleteComment(Long idComment) {
        this.commentRepository.deleteById(idComment);
        this.reviewEventProducer.sendReviewsDeletedEvent(Set.of(idComment));
    }
}
