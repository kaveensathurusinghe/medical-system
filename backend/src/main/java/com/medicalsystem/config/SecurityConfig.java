package com.medicalsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register/**", "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/doctor-categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/today-at-a-glance").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/doctors/register").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/doctors/*/dashboard-stats", "/api/doctors/*/appointments")
                    .hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/doctors/*/change-password")
                    .hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/doctors/**")
                    .hasAnyRole("PATIENT", "DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/doctors/**")
                    .hasAnyRole("DOCTOR", "ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/patients").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/patients/register").permitAll()
                .requestMatchers("/api/patients/**").hasAnyRole("PATIENT", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/appointments/book").hasRole("PATIENT")
                .requestMatchers(HttpMethod.DELETE, "/api/appointments/**").hasRole("ADMIN")
                .requestMatchers("/api/appointments/**").hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/timeslots/doctor/*/available").hasAnyRole("PATIENT", "DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/timeslots/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/timeslots/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/timeslots/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/timeslots/**").hasAnyRole("DOCTOR", "ADMIN")

                .requestMatchers("/api/payments/**").hasAnyRole("PATIENT", "ADMIN", "DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/records/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/records/**").hasAnyRole("PATIENT", "DOCTOR", "ADMIN")

                .requestMatchers("/", "/register/**", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden"))
                )
            .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}