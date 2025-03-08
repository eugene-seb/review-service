package com.eugene.review_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("USER-SERVICE")
public interface UserFeign {

    @GetMapping("user/exists/{username}")
    ResponseEntity<Boolean> isUserExist(@PathVariable String username);
}
