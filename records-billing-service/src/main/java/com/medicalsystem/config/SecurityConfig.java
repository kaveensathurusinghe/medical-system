package com.medicalsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
            // Use Keycloak username (email in this project) as principal so existing
            // authorization lookups by email keep working.
            jwtAuthConverter.setPrincipalClaimName("preferred_username");
            jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
                List<GrantedAuthority> authorities = new ArrayList<>();
                Object realmAccessObj = jwt.getClaimAsMap("realm_access");
                if (realmAccessObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String,Object> realmAccess = (java.util.Map<String,Object>) realmAccessObj;
                    Object rolesObj = realmAccess.get("roles");
                    if (rolesObj instanceof Collection) {
                        for (Object r : (Collection<?>) rolesObj) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase()));
                        }
                    }
                }
                return authorities;
            });

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated()
                )
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}