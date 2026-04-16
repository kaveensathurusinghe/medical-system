package com.medicalsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MedicalRecordService {
    private final RestTemplate restTemplate;

    @Value("${records.service.base-url:http://records-billing-service:8085/api}")
    private String recordsBaseUrl;

    public MedicalRecordService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public long countRecordsByPatientId(Long patientId) {
        try {
            String url = recordsBaseUrl + "/records/patient/" + patientId + "/count";
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return toLong(resp.getBody() == null ? null : resp.getBody().get("count"));
        } catch (RestClientResponseException ex) {
            return 0L;
        }
    }

    public long countRecords() {
        try {
            String url = recordsBaseUrl + "/admin/records/count";
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return toLong(resp.getBody() == null ? null : resp.getBody().get("count"));
        } catch (RestClientResponseException ex) {
            return 0L;
        }
    }

    public long getTodayRecordsCount() {
        try {
            String url = recordsBaseUrl + "/public/records/today-count";
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return toLong(resp.getBody() == null ? null : resp.getBody().get("count"));
        } catch (RestClientResponseException ex) {
            return 0L;
        }
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0L;
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