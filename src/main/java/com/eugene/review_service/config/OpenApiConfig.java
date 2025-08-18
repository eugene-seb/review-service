package com.eugene.review_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    @Value("${keycloak.authorizationUrl}")
    private String authorizationUrl;
    
    @Value("${keycloak.tokenUrl}")
    private String tokenUrl;
    
    @Bean
    public OpenAPI accessOpenAPI() {
        
        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl(this.authorizationUrl)
                .tokenUrl(this.tokenUrl)
                .scopes(new Scopes()
                                .addString("openid",
                                           "OpenID scope")
                                .addString("profile",
                                           "Profile information")
                                .addString("email",
                                           "Email information"));
        
        SecurityScheme oAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(authorizationCodeFlow));
        
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("keycloakAuth"))
                .components(new Components().addSecuritySchemes("keycloakAuth",
                                                                oAuthScheme))
                .info(new Info()
                              .title("Review service")
                              .description("This is the review service API of MF Library.")
                              .version("1.0"));
    }
}
