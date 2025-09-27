package com.eugene.review_service.controller;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController
{
    private final CommentService reviewEventProducer;
    
    @Operation(summary = "Create a comment.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<CommentDetailsDto> createComment(@RequestBody CommentDto commentDto)
            throws URISyntaxException {
        
        CommentDetailsDto commentDetailsDto = this.reviewEventProducer.createComment(commentDto);
        
        return ResponseEntity
                .created(new URI("/api/comment/" + commentDetailsDto.getId()))
                .body(commentDetailsDto);
    }
    
    @Operation(summary = "Get the comments of a book.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<CommentDetailsDto>> getCommentsByBook(@PathVariable String bookId) {
        return ResponseEntity.ok(this.reviewEventProducer.getCommentsByBook(bookId));
    }
    
    @Operation(summary = "Get a comment by ID.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{idComment}")
    public ResponseEntity<CommentDetailsDto> getCommentById(@PathVariable Long idComment) {
        return ResponseEntity.ok(this.reviewEventProducer.getCommentById(idComment));
    }
    
    @Operation(summary = "Update a comment.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update")
    public ResponseEntity<CommentDetailsDto> updateComment(@RequestBody CommentDetailsDto commentDetailsDto) {
        return ResponseEntity.ok(this.reviewEventProducer.updateComment(commentDetailsDto));
    }
    
    @Operation(summary = "Delete a comment.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        this.reviewEventProducer.deleteComment(commentId);
        
        return ResponseEntity
                .ok()
                .build();
    }
}
