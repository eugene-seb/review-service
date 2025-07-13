package com.eugene.review_service.unit.controller;

import com.eugene.review_service.controller.RateController;
import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.service.RateService;
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

@WebMvcTest(RateController.class)
@ActiveProfiles("test")
class RateControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RateService rateService;

    @Test
    void getRatesByBook() throws Exception {
        List<RateDetailsDto> rates = List.of(new RateDetailsDto(5L, 4, LocalDateTime
                .now()
                .toString(), "String userId", "String bookId"));

        given(rateService.getRatesByBook("String bookId")).willReturn(rates);

        mockMvc
                .perform(get("/rate/rates/book/{bookId}", "String bookId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(rates.size()));

        verify(rateService).getRatesByBook("String bookId");
    }
}
