package com.eugene.review_service.controller;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("comment")
public class CommentController
{
    private final CommentService reviewEventProducer;

    public CommentController(CommentService reviewEventProducer) {
        this.reviewEventProducer = reviewEventProducer;
    }

    @PostMapping("create_comment")
    public ResponseEntity<CommentDetailsDto> createComment(@Valid @RequestBody CommentDto commentDto)
            throws URISyntaxException {

        CommentDetailsDto commentDetailsDto = this.reviewEventProducer.createComment(commentDto);

        return ResponseEntity.created(new URI("/comment?idComment=" + commentDetailsDto.getId()))
                             .body(commentDetailsDto);
    }

    @GetMapping("comments/book/{bookId}")
    public ResponseEntity<List<CommentDetailsDto>> getCommentsByBook(@PathVariable String bookId) {
        return ResponseEntity.ok(this.reviewEventProducer.getCommentsByBook(bookId));
    }

    @GetMapping
    public ResponseEntity<CommentDetailsDto> getCommentById(@RequestParam Long idComment) {
        return ResponseEntity.ok(this.reviewEventProducer.getCommentById(idComment));
    }

    @PutMapping("/update")
    public ResponseEntity<CommentDetailsDto> updateComment(@Valid @RequestBody CommentDetailsDto commentDetailsDto) {
        return ResponseEntity.ok(this.reviewEventProducer.updateComment(commentDetailsDto));
    }

    @DeleteMapping("delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        this.reviewEventProducer.deleteComment(commentId);

        return ResponseEntity.ok()
                             .build();
    }
}
