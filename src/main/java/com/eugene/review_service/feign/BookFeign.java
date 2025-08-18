package com.eugene.review_service.feign;

import com.eugene.review_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOK-SERVICE", configuration = FeignConfig.class)
public interface BookFeign
{
    @GetMapping("api/book/exists/{bookId}")
    ResponseEntity<Boolean> isBookExist(@PathVariable String bookId);
}
