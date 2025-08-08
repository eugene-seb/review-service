package com.eugene.review_service.controller;

import com.eugene.review_service.dto.RateDetailsDto;
import com.eugene.review_service.dto.RateDto;
import com.eugene.review_service.service.RateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/rate")
@RequiredArgsConstructor
public class RateController
{
    private final RateService rateService;
    
    @Operation(summary = "Create or update a rate.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createorupdate")
    public ResponseEntity<RateDetailsDto> createOrUpdateRate(@Valid @RequestBody RateDto rateDto)
            throws URISyntaxException {
        RateDetailsDto rateDetailsDto = this.rateService.createOrUpdateRate(rateDto);
        
        return ResponseEntity
                .created(new URI("/api/rate/" + rateDetailsDto.getId()))
                .body(rateDetailsDto);
    }
    
    @Operation(summary = "Get all the rates on a book.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<RateDetailsDto>> getRatesByBook(@PathVariable String bookId) {
        return ResponseEntity.ok(this.rateService.getRatesByBook(bookId));
    }
    
    @Operation(summary = "Get a rate by ID.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{rateId}")
    public ResponseEntity<RateDetailsDto> getRateById(@PathVariable Long rateId) {
        return ResponseEntity.ok(this.rateService.getRateById(rateId));
    }
    
    @Operation(summary = "Delete a rate.")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{rateId}")
    public ResponseEntity<Void> deleteRate(@PathVariable Long rateId) {
        this.rateService.deleteRate(rateId);
        
        return ResponseEntity
                .ok()
                .build();
    }
}
