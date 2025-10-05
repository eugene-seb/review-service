package com.eugene.review_service.functional;

import com.eugene.review_service.dto.CommentDetailsDto;
import com.eugene.review_service.dto.CommentDto;
import com.eugene.review_service.feign.BookFeign;
import com.eugene.review_service.feign.UserFeign;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CommentControllerFunctionalTest
{
    private final CommentDto commentDto;
    private final CommentDto commentDto2;
    private final CommentDetailsDto commentDetailsDto;
    private final CommentDetailsDto updatedCommentDetailsDto;
    
    /**
     * I don't want the context to load kafka for this test, so I'm mocking his initialization
     * It will replace all the KafkaTemplate instances.
     */
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;
    @MockitoBean
    private UserFeign userFeign;
    @MockitoBean
    private BookFeign bookFeign;
    
    @Autowired
    private MockMvc mockMvc;
    
    public CommentControllerFunctionalTest() {
        this.commentDto = new CommentDto("Great book!",
                                         "user1",
                                         "book1");
        this.commentDto2 = new CommentDto("Amazing content!",
                                          "user2",
                                          "book1");
        this.commentDetailsDto = new CommentDetailsDto(1L,
                                                       "Great book!",
                                                       LocalDateTime.now(),
                                                       "user1",
                                                       "book1");
        this.updatedCommentDetailsDto = new CommentDetailsDto(1L,
                                                              "Updated comment content!",
                                                              LocalDateTime.now(),
                                                              "user1",
                                                              "book1");
    }
    
    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            // Enable the support of LocalDateTime for JSON serialization/deserialization)
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Autowired
    public void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    @Order(1)
    void createComment_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        when(this.userFeign.isUserExist(anyString())).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist(anyString())).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.commentDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.userFeign,
               never()).isUserExist(anyString());
        verify(this.bookFeign,
               never()).isBookExist(anyString());
    }
    
    @Test
    @Order(2)
    @WithMockUser
    void createComment_withValidData_shouldSucceed() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.commentDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/comment/1"))
                .andExpect(jsonPath("$.userId").value(this.commentDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(this.commentDto.getBookId()))
                .andExpect(jsonPath("$.content").value(this.commentDto.getContent()));
        
        verify(this.userFeign).isUserExist("user1");
        verify(this.bookFeign).isBookExist("book1");
    }
    
    @Test
    @Order(3)
    @WithMockUser
    void createComment_withNonExistingUser_shouldReturnNotFound() throws Exception {
        when(this.userFeign.isUserExist("user3")).thenReturn(ResponseEntity.ok(false));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        CommentDto invalidCommentDto = new CommentDto("Content",
                                                      "user3",
                                                      "book1");
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.userFeign).isUserExist("user3");
        verify(this.bookFeign).isBookExist("book1");
    }
    
    @Test
    @Order(4)
    @WithMockUser
    void createComment_withNonExistingBook_shouldReturnNotFound() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book99")).thenReturn(ResponseEntity.ok(false));
        
        CommentDto invalidCommentDto = new CommentDto("Content",
                                                      "user1",
                                                      "book99");
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.userFeign).isUserExist("user1");
        verify(this.bookFeign).isBookExist("book99");
    }
    
    @Test
    @Order(5)
    @WithMockUser
    void createComment_secondComment_shouldSucceed() throws Exception {
        when(this.userFeign.isUserExist("user2")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.commentDto2))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/comment/2"))
                .andExpect(jsonPath("$.userId").value(this.commentDto2.getUserId()))
                .andExpect(jsonPath("$.content").value(this.commentDto2.getContent()));
    }
    
    @Test
    @Order(6)
    @WithMockUser
    void createComment_withBlankContent_shouldReturnBadRequest() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        CommentDto invalidCommentDto = new CommentDto("",
                                                      "user1",
                                                      "book1");
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.userFeign,
               never()).isUserExist(anyString());
        verify(this.bookFeign,
               never()).isBookExist(anyString());
    }
    
    @Test
    @Order(7)
    void getCommentsByBook_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(8)
    @WithMockUser
    void getCommentsByBook_withExistingBook_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].bookId").value("book1"))
                .andExpect(jsonPath("$[1].bookId").value("book1"));
    }
    
    @Test
    @Order(9)
    @WithMockUser
    void getCommentsByBook_withNonExistingBook_shouldReturnEmptyList() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
    
    @Test
    @Order(10)
    void getCommentById_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             1))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(11)
    @WithMockUser
    void getCommentById_withValidId_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value(this.commentDto.getContent()))
                .andExpect(jsonPath("$.userId").value(this.commentDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(this.commentDto.getBookId()));
    }
    
    @Test
    @Order(12)
    @WithMockUser
    void getCommentById_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             999))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(13)
    void updateComment_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.updatedCommentDetailsDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(14)
    @WithMockUser
    void updateComment_withValidData_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.updatedCommentDetailsDto))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value(this.updatedCommentDetailsDto.getContent()))
                .andExpect(jsonPath("$.userId").value(this.updatedCommentDetailsDto.getUserId()));
    }
    
    @Test
    @Order(15)
    @WithMockUser
    void updateComment_withNonExistingId_shouldReturnNotFound() throws Exception {
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
    }
    
    @Test
    @Order(16)
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
    }
    
    @Test
    @Order(17)
    void deleteComment_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                2).with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(18)
    @WithMockUser
    void deleteComment_withValidId_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                2).with(csrf()))
                .andExpect(status().isOk());
        
        // Verify the comment is deleted
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             2))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(19)
    @WithMockUser
    void deleteComment_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                999).with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(20)
    @WithMockUser
    void deleteComment_finalCleanup() throws Exception {
        // Clean up remaining comment
        this.mockMvc
                .perform(delete("/api/comment/delete/{commentId}",
                                1).with(csrf()))
                .andExpect(status().isOk());
        
        // Verify no comments left for the book
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
    
    // EDGE CASE TESTS
    
    @Test
    @Order(21)
    @WithMockUser
    void createComment_withNullContent_shouldReturnBadRequest() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        // Create comment with null content using reflection or builder if available
        CommentDto invalidCommentDto = new CommentDto(null,
                                                      "user1",
                                                      "book1");
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidCommentDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(22)
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
    }
    
    @Test
    @Order(23)
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
    }
}