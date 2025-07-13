package com.eugene.review_service.controller;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.service.RateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
        RateDetailsDto rateDetailsDto = rateService.createOrUpdateRate(rateDto);

        return ResponseEntity
                .created(new URI("/rate?idRate=" + rateDetailsDto.id()))
                .body(rateDetailsDto);
    }

    @GetMapping("rates/book/{bookId}")
    public ResponseEntity<List<RateDetailsDto>> getRatesByBook(@PathVariable String bookId) {
        return ResponseEntity.ok(rateService.getRatesByBook(bookId));
    }

    @GetMapping
    public ResponseEntity<RateDetailsDto> getRateById(@RequestParam Long idRate) {
        return ResponseEntity.ok(rateService.getRateById(idRate));
    }

    @DeleteMapping("delete/{rateId}")
    public ResponseEntity<Void> deleteRate(@PathVariable Long rateId) {
        rateService.deleteRate(rateId);

        return ResponseEntity
                .ok()
                .build();
    }
}
