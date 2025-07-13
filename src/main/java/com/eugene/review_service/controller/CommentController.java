package com.eugene.review_service.controller;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

        CommentDetailsDto commentDetailsDto = commentService.createComment(commentDto);

        return ResponseEntity
                .created(new URI("/comment?idComment=" + commentDetailsDto.id()))
                .body(commentDetailsDto);
    }

    @GetMapping("comments/book/{bookId}")
    public ResponseEntity<List<CommentDetailsDto>> getCommentsByBook(@PathVariable String bookId) {
        return ResponseEntity.ok(commentService.getCommentsByBook(bookId));
    }

    @GetMapping
    public ResponseEntity<CommentDetailsDto> getCommentById(@RequestParam Long idComment) {
        return ResponseEntity.ok(commentService.getCommentById(idComment));
    }

    @PutMapping("/update")
    public ResponseEntity<CommentDetailsDto> updateComment(
            @RequestBody CommentDetailsDto commentDetailsDto) {
        return ResponseEntity.ok(commentService.updateComment(commentDetailsDto));
    }

    @DeleteMapping("delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);

        return ResponseEntity
                .ok()
                .build();
    }
}
