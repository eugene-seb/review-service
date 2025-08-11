package com.eugene.review_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtConverterProperties.class)
@RequiredArgsConstructor
public class SecurityConfig
{
    private final JwtConverterProperties jwtConverterProperties;
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtConverter jwtConverter
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/rate/createorupdate/**")
                        .authenticated()
                        
                        .requestMatchers("/api/rate/delete/**")
                        .authenticated()
                        
                        .requestMatchers("/api/comment/create/**")
                        .authenticated()
                        
                        .requestMatchers("/api/comment/update/**")
                        .authenticated()
                        
                        .requestMatchers("/api/comment/delete/**")
                        .authenticated()
                        
                        .requestMatchers("/swagger-ui/**",
                                         "/v3/api-docs/**",
                                         "/swagger-ui/oauth2-redirect.html")
                        .permitAll()
                        
                        .anyRequest()
                        .authenticated())
                
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                
                // I don't need to handle the session in the backend because I use JWT Token from Keycloak.
                // Therefore, I don't need to manage the session thank to keycloak which deal with the expiration of the tokens.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // CSRF protection is disabled because this application is a stateless REST API
                // using JWT tokens for authentication. CSRF mainly targets session-based app
                // with cookies automatically sent by browsers, which is not applicable here.
                .csrf(AbstractHttpConfigurer::disable)
                
                // The frontend will be in a different domain
                .cors(Customizer.withDefaults());
        
        return http.build();
    }
    
    @Bean
    public JwtConverter jwtConverter() {
        return new JwtConverter(jwtConverterProperties);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://my-frontend.com"));
        configuration.setAllowedMethods(List.of("GET",
                                                "POST",
                                                "PUT",
                                                "DELETE",
                                                "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",
                                         configuration);
        return source;
    }
}
