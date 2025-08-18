package com.eugene.review_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt.auth.converter")
public class JwtConverterProperties
{
    private String resourceId;
    private String principalAttribute;
}
