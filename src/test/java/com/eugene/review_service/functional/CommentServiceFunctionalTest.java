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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CommentServiceFunctionalTest
{
    private final CommentDto commentDto;
    private final CommentDetailsDto commentDetailsDto;
    
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
    
    public CommentServiceFunctionalTest() {
        this.commentDto = new CommentDto("String comment",
                                         "user1",
                                         "book4");
        this.commentDetailsDto = new CommentDetailsDto(1L,
                                                       "String comment",
                                                       LocalDateTime.now(),
                                                       "user1",
                                                       "book4");
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
    @WithMockUser
    void createComment() throws Exception {
        
        when(this.userFeign.isUserExist(anyString())).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist(anyString())).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/comment/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(this.commentDto.getUserId()));
        
        verify(this.userFeign).isUserExist(anyString());
        verify(this.bookFeign).isBookExist(anyString());
    }
    
    @Test
    @Order(2)
    @WithMockUser
    void getCommentsByBook() throws Exception {
        
        this.mockMvc
                .perform(get("/api/comment/book/{bookId}",
                             this.commentDto.getBookId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
    
    @Test
    @Order(3)
    @WithMockUser
    void getCommentById() throws Exception {
        
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(this.commentDto.getContent()));
    }
    
    @Test
    @Order(4)
    @WithMockUser
    void updateComment() throws Exception {
        
        this.mockMvc
                .perform(put("/api/comment/update")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.commentDetailsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(this.commentDetailsDto.getContent()));
    }
    
    @Test
    @Order(5)
    @WithMockUser
    void deleteComment() throws Exception {
        
        this.mockMvc
                .perform(delete("/api/comment/delete/{idComment}",
                                1))
                .andExpect(status().isOk());
        
        this.mockMvc
                .perform(get("/api/comment/{idComment}",
                             1))
                .andExpect(status().isNotFound());
    }
}
