package com.medicalsystem.appointments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setPrincipalClaimName("sub");
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            Object directRole = jwt.getClaimAsString("role");
            if (directRole != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + directRole.toString().toUpperCase()));
                return authorities;
            }
            Object realmAccessObj = jwt.getClaimAsMap("realm_access");
            if (realmAccessObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> realmAccess = (java.util.Map<String, Object>) realmAccessObj;
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
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String jwtSecret) {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
