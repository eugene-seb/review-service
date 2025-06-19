package com.eugene.review_service.controller;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.service.RateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("rate")
public class RateController {
    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping("createorupdate_rate")
    public ResponseEntity<RateDetailsDto> createRate(@RequestBody RateDto rateDto) throws
            URISyntaxException {
        return rateService.createOrUpdateRate(rateDto);
    }

    @GetMapping("rates/book/{bookId}")
    public ResponseEntity<List<RateDetailsDto>> getRatesByBook(@PathVariable String bookId) {
        return rateService.getRatesByBook(bookId);
    }

    @GetMapping
    public ResponseEntity<RateDetailsDto> getRateById(@RequestParam Long idRate) {
        return rateService.getRateById(idRate);
    }

    @DeleteMapping("delete/{rateId}")
    public ResponseEntity<Void> deleteRate(@PathVariable Long rateId) {
        return rateService.deleteRate(rateId);
    }
}
