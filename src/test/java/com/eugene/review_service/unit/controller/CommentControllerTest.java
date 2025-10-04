package com.eugene.review_service.unit.controller;

import com.eugene.review_service.config.SecurityConfig;
import com.eugene.review_service.controller.CommentController;
import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.exception.NotFoundException;
import com.eugene.review_service.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class CommentControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private CommentService commentService;
    
    private CommentDto commentDto;
    private CommentDetailsDto commentDetailsDto;
    private CommentDetailsDto updatedCommentDetailsDto;
    
    static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @BeforeEach
    void setUp() {
        this.commentDto = new CommentDto("Great book review!",
                                         "user1",
                                         "book1");
        this.commentDetailsDto = new CommentDetailsDto(1L,
                                                       "Great book review!",
                                                       LocalDateTime.now(),
                                                       "user1",
                                                       "book1");
        this.updatedCommentDetailsDto = new CommentDetailsDto(1L,
                                                              "Updated comment content!",
                                                              LocalDateTime.now(),
                                                              "user1",
                                                              "book1");
    }
    
    @Test
    @WithMockUser
    void createComment_withValidData_shouldSucceed() throws Exception {
        given(this.commentService.createComment(any(CommentDto.class))).willReturn(commentDetailsDto);
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(commentDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/comment/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value(commentDto.getContent()))
                .andExpect(jsonPath("$.userId").value(commentDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(commentDto.getBookId()));
        
        verify(this.commentService).createComment(any(CommentDto.class));
    }
    
    @Test
    @WithMockUser
    void createComment_withNonExistingUserOrBook_shouldReturnNotFound() throws Exception {
        given(this.commentService.createComment(any(CommentDto.class))).willThrow(new NotFoundException("The user or book do not exist.",
                                                                                                        null));
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(commentDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.commentService).createComment(any(CommentDto.class));
    }
    
    @Test
    void createComment_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(commentDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.commentService,
               never()).createComment(any(CommentDto.class));
    }
    
    @Test
    @WithMockUser
    void createComment_withBlankContent_shouldReturnBadRequest() throws Exception {
        CommentDto invalidCommentDto = new CommentDto("",
                                                      "user1",
                                                      "book1");
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.commentService,
               never()).createComment(any(CommentDto.class));
    }
    
    @Test
    @WithMockUser
    void createComment_withNullUserId_shouldReturnBadRequest() throws Exception {
        CommentDto invalidCommentDto = new CommentDto("Content",
                                                      null,
                                                      "book1");
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.commentService,
               never()).createComment(any(CommentDto.class));
    }
    
    @Test
    @WithMockUser
    void getCommentsByBook_withExistingBook_shouldSucceed() throws Exception {
        List<CommentDetailsDto> comments = List.of(new CommentDetailsDto(1L,
                                                                         "Great book!",
                                                                         LocalDateTime.now(),
                                                                         "user1",
                                                                         "book1"),
                                                   new CommentDetailsDto(2L,
                                                                         "Amazing content!",
                                                                         LocalDateTime.now(),
                                                                         "user2",
                                                                         "book1"));
        
        given(this.commentService.getCommentsByBook("book1")).willReturn(comments);
        
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(comments.size()))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookId").value("book1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].bookId").value("book1"));
        
        verify(this.commentService).getCommentsByBook("book1");
    }
    
    @Test
    @WithMockUser
    void getCommentsByBook_withNoComments_shouldReturnEmptyList() throws Exception {
        given(this.commentService.getCommentsByBook("book99")).willReturn(List.of());
        
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
        
        verify(this.commentService).getCommentsByBook("book99");
    }
    
    @Test
    void getCommentsByBook_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book1"))
                .andExpect(status().isUnauthorized());
        
        verify(this.commentService,
               never()).getCommentsByBook(anyString());
    }
    
    @Test
    @WithMockUser
    void getCommentById_withValidId_shouldSucceed() throws Exception {
        given(this.commentService.getCommentById(1L)).willReturn(commentDetailsDto);
        
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value(commentDetailsDto.getContent()))
                .andExpect(jsonPath("$.userId").value(commentDetailsDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(commentDetailsDto.getBookId()));
        
        verify(this.commentService).getCommentById(1L);
    }
    
    @Test
    @WithMockUser
    void getCommentById_withNonExistingId_shouldReturnNotFound() throws Exception {
        given(this.commentService.getCommentById(999L)).willThrow(new NotFoundException("Comment '999' not found.",
                                                                                        null));
        
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             999))
                .andExpect(status().isNotFound());
        
        verify(this.commentService).getCommentById(999L);
    }
    
    @Test
    void getCommentById_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             1))
                .andExpect(status().isUnauthorized());
        
        verify(this.commentService,
               never()).getCommentById(anyLong());
    }
    
    @Test
    @WithMockUser
    void updateComment_withValidData_shouldSucceed() throws Exception {
        given(this.commentService.updateComment(any(CommentDetailsDto.class))).willReturn(updatedCommentDetailsDto);
        
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(updatedCommentDetailsDto))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value(updatedCommentDetailsDto.getContent()))
                .andExpect(jsonPath("$.userId").value(updatedCommentDetailsDto.getUserId()));
        
        verify(this.commentService).updateComment(any(CommentDetailsDto.class));
    }
    
    @Test
    @WithMockUser
    void updateComment_withNonExistingId_shouldReturnNotFound() throws Exception {
        given(this.commentService.updateComment(any(CommentDetailsDto.class))).willThrow(new NotFoundException("Comment '999' not found.",
                                                                                                               null));
        
        CommentDetailsDto nonExistingComment = new CommentDetailsDto(999L,
                                                                     "Content",
                                                                     LocalDateTime.now(),
                                                                     "user1",
                                                                     "book1");
        
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(nonExistingComment))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.commentService).updateComment(any(CommentDetailsDto.class));
    }
    
    @Test
    void updateComment_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(updatedCommentDetailsDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.commentService,
               never()).updateComment(any(CommentDetailsDto.class));
    }
    
    @Test
    @WithMockUser
    void updateComment_withBlankContent_shouldReturnBadRequest() throws Exception {
        CommentDetailsDto invalidComment = new CommentDetailsDto(1L,
                                                                 "",
                                                                 LocalDateTime.now(),
                                                                 "user1",
                                                                 "book1");
        
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidComment))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.commentService,
               never()).updateComment(any(CommentDetailsDto.class));
    }
    
    @Test
    @WithMockUser
    void deleteComment_withValidId_shouldSucceed() throws Exception {
        doNothing()
                .when(this.commentService)
                .deleteComment(1L);
        
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                1).with(csrf()))
                .andExpect(status().isOk());
        
        verify(this.commentService).deleteComment(1L);
    }
    
    @Test
    @WithMockUser
    void deleteComment_withNonExistingId_shouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("Comment '999' not found.",
                                      null))
                .when(this.commentService)
                .deleteComment(999L);
        
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                999).with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.commentService).deleteComment(999L);
    }
    
    @Test
    void deleteComment_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                1).with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.commentService,
               never()).deleteComment(anyLong());
    }
    
    // EDGE CASE TESTS
    
    @Test
    @WithMockUser
    void createComment_withNullBookId_shouldReturnBadRequest() throws Exception {
        CommentDto invalidCommentDto = new CommentDto("Content",
                                                      "user1",
                                                      null);
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.commentService,
               never()).createComment(any(CommentDto.class));
    }
    
    @Test
    @WithMockUser
    void updateComment_withNullContent_shouldReturnBadRequest() throws Exception {
        CommentDetailsDto invalidComment = new CommentDetailsDto(1L,
                                                                 null,
                                                                 LocalDateTime.now(),
                                                                 "user1",
                                                                 "book1");
        
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidComment))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.commentService,
               never()).updateComment(any(CommentDetailsDto.class));
    }
}