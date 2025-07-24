package com.eugene.review_service.controller;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.service.RateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("rate")
public class RateController
{
    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping("createorupdate_rate")
    public ResponseEntity<RateDetailsDto> createRate(@Valid @RequestBody RateDto rateDto)
            throws URISyntaxException {
        RateDetailsDto rateDetailsDto = this.rateService.createOrUpdateRate(rateDto);

        return ResponseEntity.created(new URI("/rate?idRate=" + rateDetailsDto.getId()))
                             .body(rateDetailsDto);
    }

    @GetMapping("rates/book/{bookId}")
    public ResponseEntity<List<RateDetailsDto>> getRatesByBook(@PathVariable String bookId) {
        return ResponseEntity.ok(this.rateService.getRatesByBook(bookId));
    }

    @GetMapping
    public ResponseEntity<RateDetailsDto> getRateById(@RequestParam Long idRate) {
        return ResponseEntity.ok(this.rateService.getRateById(idRate));
    }

    @DeleteMapping("delete/{rateId}")
    public ResponseEntity<Void> deleteRate(@PathVariable Long rateId) {
        this.rateService.deleteRate(rateId);

        return ResponseEntity.ok()
                             .build();
    }
}
