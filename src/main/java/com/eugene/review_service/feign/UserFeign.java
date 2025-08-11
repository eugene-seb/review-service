package com.eugene.review_service.feign;

import com.eugene.review_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", configuration = FeignConfig.class)
public interface UserFeign
{
    @GetMapping("api/user/exists/{userId}")
    ResponseEntity<Boolean> isUserExist(@PathVariable String userId);
}
