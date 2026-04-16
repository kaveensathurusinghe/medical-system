package com.medicalsystem.service;

import com.medicalsystem.model.Payment;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private final RestTemplate restTemplate;

    @Value("${records.service.base-url:http://records-billing-service:8085/api}")
    private String recordsBaseUrl;

    public PaymentService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<Payment> getPaymentsByPatientId(String patientId) {
        String url = recordsBaseUrl + "/payments/patient/" + patientId;
        return getPaymentList(url);
    }

    public List<Payment> getPaymentsByDoctorId(String doctorId) {
        String url = recordsBaseUrl + "/payments/doctor/" + doctorId;
        return getPaymentList(url);
    }

    public double getTotalIncomeByDoctorId(String doctorId) {
        try {
            String url = recordsBaseUrl + "/payments/doctor/" + doctorId + "/income";
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return toDouble(resp.getBody() == null ? null : resp.getBody().get("totalIncome"));
        } catch (RestClientResponseException ex) {
            return 0.0;
        }
    }

    public List<Payment> getAllPayments() {
        String url = recordsBaseUrl + "/admin/payments";
        return getPaymentList(url);
    }

    public List<Payment> getRecentPayments(int limit) {
        String url = recordsBaseUrl + "/admin/recent-payments?limit=" + limit;
        return getPaymentList(url);
    }

    public double getTotal() {
        return toDouble(getBillingSummary().get("paymentTotal"));
    }

    public double getTotalDoctorCharges() {
        return toDouble(getBillingSummary().get("doctorChargesTotal"));
    }

    public double getTodayPaymentTotal() {
        try {
            String url = recordsBaseUrl + "/public/payments/today-total";
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return toDouble(resp.getBody() == null ? null : resp.getBody().get("paymentTotal"));
        } catch (RestClientResponseException ex) {
            return 0.0;
        }
    }

    private Map<String, Object> getBillingSummary() {
        try {
            String url = recordsBaseUrl + "/admin/payments/summary";
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return resp.getBody() == null ? Collections.emptyMap() : resp.getBody();
        } catch (RestClientResponseException ex) {
            return Collections.emptyMap();
        }
    }

    private List<Payment> getPaymentList(String url) {
        try {
            ResponseEntity<List<Payment>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<List<Payment>>() {}
            );
            return resp.getBody() == null ? Collections.emptyList() : resp.getBody();
        } catch (RestClientResponseException ex) {
            return Collections.emptyList();
        }
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
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

    private <T> HttpEntity<T> authEntity(T body) {
        return new HttpEntity<>(body, authHeaders());
    }
}