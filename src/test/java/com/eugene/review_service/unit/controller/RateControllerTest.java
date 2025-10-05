package com.eugene.review_service.unit.controller;

import com.eugene.review_service.config.SecurityConfig;
import com.eugene.review_service.controller.RateController;
import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.exception.NotFoundException;
import com.eugene.review_service.service.RateService;
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

@WebMvcTest(RateController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class RateControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private RateService rateService;
    
    private RateDto rateDto;
    private RateDto updateRateDto;
    private RateDetailsDto rateDetailsDto;
    private RateDetailsDto updatedRateDetailsDto;
    
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
        this.rateDto = new RateDto(4,
                                   "user1",
                                   "book1");
        this.updateRateDto = new RateDto(2,
                                         "user1",
                                         "book1");
        this.rateDetailsDto = new RateDetailsDto(1L,
                                                 4,
                                                 LocalDateTime.now(),
                                                 "user1",
                                                 "book1");
        this.updatedRateDetailsDto = new RateDetailsDto(1L,
                                                        2,
                                                        LocalDateTime.now(),
                                                        "user1",
                                                        "book1");
    }
    
    @Test
    @WithMockUser
    void createOrUpdateRate_withValidData_shouldSucceed() throws Exception {
        given(this.rateService.createOrUpdateRate(any(RateDto.class))).willReturn(rateDetailsDto);
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(rateDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/rate/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(rateDto.getScore()))
                .andExpect(jsonPath("$.userId").value(rateDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(rateDto.getBookId()));
        
        verify(this.rateService).createOrUpdateRate(any(RateDto.class));
    }
    
    @Test
    @WithMockUser
    void createOrUpdateRate_updateExistingRate_shouldSucceed() throws Exception {
        given(this.rateService.createOrUpdateRate(any(RateDto.class))).willReturn(updatedRateDetailsDto);
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(updateRateDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(updateRateDto.getScore())); // Updated score
        
        verify(this.rateService).createOrUpdateRate(any(RateDto.class));
    }
    
    @Test
    @WithMockUser
    void createOrUpdateRate_withNonExistingUserOrBook_shouldReturnNotFound() throws Exception {
        given(this.rateService.createOrUpdateRate(any(RateDto.class))).willThrow(new NotFoundException("The user or book do not exist.",
                                                                                                       null));
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(rateDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.rateService).createOrUpdateRate(any(RateDto.class));
    }
    
    @Test
    void createOrUpdateRate_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(rateDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.rateService,
               never()).createOrUpdateRate(any(RateDto.class));
    }
    
    @Test
    @WithMockUser
    void createOrUpdateRate_withInvalidScoreTooHigh_shouldReturnBadRequest() throws Exception {
        RateDto invalidRateDto = new RateDto(6,
                                             "user1",
                                             "book1");
        
        this.mockMvc
                .perform(post("/api/rate/createorupdate")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidRateDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void createOrUpdateRate_withInvalidScoreTooLow_shouldReturnBadRequest() throws Exception {
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
    
    @Test
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
        
        verify(this.rateService,
               never()).createOrUpdateRate(any(RateDto.class));
    }
    
    @Test
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
        
        verify(this.rateService,
               never()).createOrUpdateRate(any(RateDto.class));
    }
    
    @Test
    @WithMockUser
    void getRatesByBook_withExistingBook_shouldSucceed() throws Exception {
        List<RateDetailsDto> rates = List.of(new RateDetailsDto(1L,
                                                                4,
                                                                LocalDateTime.now(),
                                                                "user1",
                                                                "book1"),
                                             new RateDetailsDto(2L,
                                                                5,
                                                                LocalDateTime.now(),
                                                                "user2",
                                                                "book1"));
        
        given(this.rateService.getRatesByBook("book1")).willReturn(rates);
        
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(rates.size()))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].bookId").value("book1"))
                .andExpect(jsonPath("$[0].score").value(4))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].bookId").value("book1"))
                .andExpect(jsonPath("$[1].score").value(5));
        
        verify(this.rateService).getRatesByBook("book1");
    }
    
    @Test
    @WithMockUser
    void getRatesByBook_withNoRates_shouldReturnEmptyList() throws Exception {
        given(this.rateService.getRatesByBook("book99")).willReturn(List.of());
        
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
        
        verify(this.rateService).getRatesByBook("book99");
    }
    
    @Test
    void getRatesByBook_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isUnauthorized());
        
        verify(this.rateService,
               never()).getRatesByBook(anyString());
    }
    
    @Test
    @WithMockUser
    void getRateById_withValidId_shouldSucceed() throws Exception {
        given(this.rateService.getRateById(1L)).willReturn(rateDetailsDto);
        
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(rateDetailsDto.getScore()))
                .andExpect(jsonPath("$.userId").value(rateDetailsDto.getUserId()))
                .andExpect(jsonPath("$.bookId").value(rateDetailsDto.getBookId()));
        
        verify(this.rateService).getRateById(1L);
    }
    
    @Test
    @WithMockUser
    void getRateById_withNonExistingId_shouldReturnNotFound() throws Exception {
        given(this.rateService.getRateById(999L)).willThrow(new NotFoundException("Rate '999' not found.",
                                                                                  null));
        
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             999))
                .andExpect(status().isNotFound());
        
        verify(this.rateService).getRateById(999L);
    }
    
    @Test
    void getRateById_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/rate/{rateId}",
                             1))
                .andExpect(status().isUnauthorized());
        
        verify(this.rateService,
               never()).getRateById(anyLong());
    }
    
    @Test
    @WithMockUser
    void deleteRate_withValidId_shouldSucceed() throws Exception {
        doNothing()
                .when(this.rateService)
                .deleteRate(1L);
        
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                1).with(csrf()))
                .andExpect(status().isOk());
        
        verify(this.rateService).deleteRate(1L);
    }
    
    @Test
    @WithMockUser
    void deleteRate_withNonExistingId_shouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("Rate '999' not found.",
                                      null))
                .when(this.rateService)
                .deleteRate(999L);
        
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                999).with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.rateService).deleteRate(999L);
    }
    
    @Test
    void deleteRate_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(delete("/api/rate/delete/{rateId}",
                                1).with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.rateService,
               never()).deleteRate(anyLong());
    }
    
    @Test
    @WithMockUser
    void getRatesByBook_withMultipleRatesSameBook_shouldReturnAll() throws Exception {
        List<RateDetailsDto> multipleRates = List.of(new RateDetailsDto(1L,
                                                                        4,
                                                                        LocalDateTime.now(),
                                                                        "user1",
                                                                        "book1"),
                                                     new RateDetailsDto(2L,
                                                                        5,
                                                                        LocalDateTime.now(),
                                                                        "user2",
                                                                        "book1"),
                                                     new RateDetailsDto(3L,
                                                                        3,
                                                                        LocalDateTime.now(),
                                                                        "user3",
                                                                        "book1"));
        
        given(this.rateService.getRatesByBook("book1")).willReturn(multipleRates);
        
        this.mockMvc
                .perform(get("/api/rate/book/{bookId}",
                             "book1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[?(@.userId == 'user1')].score").value(4))
                .andExpect(jsonPath("$[?(@.userId == 'user2')].score").value(5))
                .andExpect(jsonPath("$[?(@.userId == 'user3')].score").value(3));
        
        verify(this.rateService).getRatesByBook("book1");
    }
}