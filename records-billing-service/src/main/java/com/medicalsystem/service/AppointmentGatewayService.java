package com.medicalsystem.service;

import com.medicalsystem.model.Appointment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AppointmentGatewayService {

    private final RestTemplate restTemplate;

    @Value("${appointments.service.base-url:http://appointments-service:8083/api}")
    private String appointmentsBaseUrl;

    public AppointmentGatewayService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        try {
            String url = appointmentsBaseUrl + "/appointments/" + appointmentId;
            ResponseEntity<Appointment> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    Appointment.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            headers.setBearerAuth(jwtAuth.getToken().getTokenValue());
        }
        return headers;
    }

    private HttpEntity<Void> authEntity() {
        return new HttpEntity<>(authHeaders());
    }
}
