package com.eugene.review_service.controller;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("create_comment")
    public ResponseEntity<CommentDetailsDto> createComment(
            @RequestBody CommentDto commentDto) throws URISyntaxException {
        return commentService.createComment(commentDto);
    }

    @GetMapping("comments/book/{bookId}")
    public ResponseEntity<List<CommentDetailsDto>> getCommentsByBook(@PathVariable String bookId) {
        return commentService.getCommentsByBook(bookId);
    }

    @GetMapping
    public ResponseEntity<CommentDetailsDto> getCommentById(@RequestParam Long idComment) {
        return commentService.getCommentById(idComment);
    }

    @PutMapping("/update")
    public ResponseEntity<CommentDetailsDto> updateComment(
            @RequestBody CommentDetailsDto commentDetailsDto) {
        return commentService.updateComment(commentDetailsDto);
    }

    @DeleteMapping("delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        return commentService.deleteComment(commentId);
    }
}
