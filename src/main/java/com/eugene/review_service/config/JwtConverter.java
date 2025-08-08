package com.eugene.review_service.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class JwtConverter
        implements Converter<Jwt, AbstractAuthenticationToken>
{
    
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final JwtConverterProperties jwtConverterProperties;
    
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> defaultAuthorities = Objects.requireNonNullElse(jwtGrantedAuthoritiesConverter.convert(jwt),
                                                                                     Collections.emptyList());
        
        Collection<GrantedAuthority> authorities = Stream
                .concat(defaultAuthorities.stream(),
                        extractKeycloakRoles(jwt).stream())
                .collect(Collectors.toSet());
        
        return new JwtAuthenticationToken(jwt,
                                          authorities,
                                          getPrincipalClaimName(jwt));
    }
    
    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = jwtConverterProperties.getPrincipalAttribute();
        return (claimName != null)
                ? jwt.getClaim(claimName)
                : jwt.getClaim(JwtClaimNames.SUB);
    }
    
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        roles.addAll(getRealmRoles(jwt));
        roles.addAll(getClientRoles(jwt));
        return roles
                .stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
    
    private Set<String> getRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return Collections.emptySet();
        Object realmRolesObj = realmAccess.get("roles");
        if (!(realmRolesObj instanceof Collection<?> realmRoles)) return Collections.emptySet();
        return realmRoles
                .stream()
                .filter(Objects::nonNull)
                .filter(String.class::isInstance)
                .map(r -> (String) r)
                .collect(Collectors.toSet());
    }
    
    private Set<String> getClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) return Collections.emptySet();
        Object clientObj = resourceAccess.get(jwtConverterProperties.getResourceId());
        if (!(clientObj instanceof Map<?, ?> clientMap)) return Collections.emptySet();
        Object clientRolesObj = clientMap.get("roles");
        if (!(clientRolesObj instanceof Collection<?> clientRoles)) return Collections.emptySet();
        return clientRoles
                .stream()
                .filter(Objects::nonNull)
                .filter(String.class::isInstance)
                .map(r -> (String) r)
                .collect(Collectors.toSet());
    }
}