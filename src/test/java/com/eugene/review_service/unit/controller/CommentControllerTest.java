package com.eugene.review_service.unit.controller;

import com.eugene.review_service.controller.CommentController;
import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
class CommentControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private CommentService commentService;
    
    @Test
    @WithMockUser(roles = {"USER", "MODERATOR", "ADMIN"})
    void getCommentsByBook() throws Exception {
        List<CommentDetailsDto> comments = List.of(new CommentDetailsDto(5L,
                                                                         "String comment",
                                                                         LocalDateTime.now(),
                                                                         "String userId",
                                                                         "String bookId"));
        
        given(this.commentService.getCommentsByBook("String bookId")).willReturn(comments);
        
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "String bookId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(comments.size()));
        
        verify(this.commentService).getCommentsByBook("String bookId");
    }
}
