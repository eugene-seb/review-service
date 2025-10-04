package com.eugene.review_service.functional;

import com.eugene.review_service.dto.RateDto;
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
class RateControllerFunctionalTest
{
    private final RateDto rateDto;
    private final RateDto rateDto2;
    private final RateDto updateRateDto;
    private final RateDto invalidScoreRateDto;
    
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
    
    public RateControllerFunctionalTest() {
        this.rateDto = new RateDto(4,
                                   "user1",
                                   "book1");
        this.rateDto2 = new RateDto(5,
                                    "user2",
                                    "book1");
        this.updateRateDto = new RateDto(2,
                                         "user1",
                                         "book1"); // Update existing rate
        this.invalidScoreRateDto = new RateDto(6,
                                               "user3",
                                               "book1"); // Invalid score > 5
    }
    
    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    @Order(1)
    void createOrUpdateRate_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        when(this.userFeign.isUserExist(anyString())).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist(anyString())).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.rateDto))
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
    void createOrUpdateRate_withValidData_shouldCreateNewRate() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.rateDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/rate/1"))
                .andExpect(jsonPath("$.userId").value(this.rateDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(this.rateDto.getBookId()))
                .andExpect(jsonPath("$.score").value(this.rateDto.getScore()));
        
        verify(this.userFeign).isUserExist("user1");
        verify(this.bookFeign).isBookExist("book1");
    }
    
    @Test
    @Order(3)
    @WithMockUser
    void createOrUpdateRate_secondUserRate_shouldCreateNewRate() throws Exception {
        when(this.userFeign.isUserExist("user2")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.rateDto2))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/rate/2"))
                .andExpect(jsonPath("$.userId").value(this.rateDto2.getUserId()))
                .andExpect(jsonPath("$.score").value(this.rateDto2.getScore()));
    }
    
    @Test
    @Order(4)
    @WithMockUser
    void createOrUpdateRate_updateExistingRate_shouldUpdateScore() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.updateRateDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(this.updateRateDto.getUserId()))
                .andExpect(jsonPath("$.score").value(this.updateRateDto.getScore())) // Should be updated score (2)
                .andExpect(jsonPath("$.id").value(1)); // Same ID as original rate
    }
    
    @Test
    @Order(5)
    @WithMockUser
    void createOrUpdateRate_withNonExistingUser_shouldReturnNotFound() throws Exception {
        when(this.userFeign.isUserExist("user99")).thenReturn(ResponseEntity.ok(false));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        RateDto invalidRateDto = new RateDto(4,
                                             "user99",
                                             "book1");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.userFeign).isUserExist("user99");
        verify(this.bookFeign).isBookExist("book1");
    }
    
    @Test
    @Order(6)
    @WithMockUser
    void createOrUpdateRate_withNonExistingBook_shouldReturnNotFound() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book99")).thenReturn(ResponseEntity.ok(false));
        
        RateDto invalidRateDto = new RateDto(4,
                                             "user1",
                                             "book99");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.userFeign).isUserExist("user1");
        verify(this.bookFeign).isBookExist("book99");
    }
    
    @Test
    @Order(7)
    @WithMockUser
    void createOrUpdateRate_withInvalidScoreTooHigh_shouldReturnBadRequest() throws Exception {
        when(this.userFeign.isUserExist("user3")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book1")).thenReturn(ResponseEntity.ok(true));
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.invalidScoreRateDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(this.userFeign).isUserExist("user3");
        verify(this.bookFeign).isBookExist("book1");
    }
    
    @Test
    @Order(8)
    @WithMockUser
    void createOrUpdateRate_withInvalidScoreTooLow_shouldReturnBadRequest() throws Exception {
        RateDto invalidRateDto = new RateDto(0,
                                             "user3",
                                             "book1");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(9)
    void getRatesByBook_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(10)
    @WithMockUser
    void getRatesByBook_withExistingBook_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2)) // user1 and user2 rates
                .andExpect(jsonPath("$[0].bookId").value("book1"))
                .andExpect(jsonPath("$[1].bookId").value("book1"))
                .andExpect(jsonPath("$[?(@.userId == 'user1')].score").value(4))
                .andExpect(jsonPath("$[?(@.userId == 'user2')].score").value(5));
    }
    
    @Test
    @Order(11)
    @WithMockUser
    void getRatesByBook_withNonExistingBook_shouldReturnEmptyList() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
    
    @Test
    @Order(12)
    void getRateById_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             1))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(13)
    @WithMockUser
    void getRateById_withValidId_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(4))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.bookId").value("book1"));
    }
    
    @Test
    @Order(14)
    @WithMockUser
    void getRateById_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             999))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(15)
    void deleteRate_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                2).with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(16)
    @WithMockUser
    void deleteRate_withValidId_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                2).with(csrf()))
                .andExpect(status().isOk());
        
        // Verify the rate is deleted
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             2))
                .andExpect(status().isNotFound());
        
        // Verify only one rate remains for the book
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
    
    @Test
    @Order(17)
    @WithMockUser
    void deleteRate_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                999).with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(18)
    @WithMockUser
    void deleteRate_finalCleanup() throws Exception {
        // Clean up remaining rate
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                1).with(csrf()))
                .andExpect(status().isOk());
        
        // Verify no rates left for the book
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
    
    // EDGE CASE TESTS
    
    @Test
    @Order(19)
    @WithMockUser
    void createOrUpdateRate_withNullUserId_shouldReturnBadRequest() throws Exception {
        RateDto invalidRateDto = new RateDto(4,
                                             null,
                                             "book1");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(20)
    @WithMockUser
    void createOrUpdateRate_withNullBookId_shouldReturnBadRequest() throws Exception {
        RateDto invalidRateDto = new RateDto(4,
                                             "user1",
                                             null);
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(21)
    @WithMockUser
    void createOrUpdateRate_withNegativeScore_shouldReturnBadRequest() throws Exception {
        // Create rate with null score using reflection if needed, or adjust constructor
        // This depends on how your RateDto is structured
        RateDto invalidRateDto = new RateDto(-1,
                                             "user1",
                                             "book1");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    // TEST FOR SAME USER DIFFERENT BOOKS
    @Test
    @Order(22)
    @WithMockUser
    void createOrUpdateRate_sameUserDifferentBooks_shouldCreateSeparateRates() throws Exception {
        when(this.userFeign.isUserExist("user1")).thenReturn(ResponseEntity.ok(true));
        when(this.bookFeign.isBookExist("book2")).thenReturn(ResponseEntity.ok(true));
        
        RateDto rateForDifferentBook = new RateDto(5,
                                                   "user1",
                                                   "book2");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(rateForDifferentBook))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.bookId").value("book2"))
                .andExpect(jsonPath("$.score").value(5));
        
        // Clean up
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                3).with(csrf()))
                .andExpect(status().isOk());
    }
}