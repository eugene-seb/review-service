package com.eugene.review_service.unit.controller;

import com.eugene.review_service.controller.CommentController;
import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CommentService commentService;

    @Test
    void getCommentsByBook() throws Exception {
        List<CommentDetailsDto> comments = List.of(new CommentDetailsDto(5L, "String comment",
                LocalDateTime
                        .now()
                        .toString(), "String userId", "String bookId"));

        given(commentService.getCommentsByBook("String bookId")).willReturn(comments);

        mockMvc
                .perform(get("/comment/comments/book/{bookId}", "String bookId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(comments.size()));

        verify(commentService).getCommentsByBook("String bookId");
    }
}
