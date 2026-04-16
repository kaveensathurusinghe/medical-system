package com.medicalsystem.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.base-url:http://keycloak:8080}")
    private String keycloakBaseUrl;

    @Value("${keycloak.realm:medicalsystem}")
    private String realm;

    @Value("${keycloak.admin-realm:master}")
    private String adminRealm;

    @Value("${keycloak.admin-client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    public void createUserWithRole(String username, String password, String fullName, String role) {
        String accessToken = getAdminAccessToken();
        String userId = createUser(accessToken, username, password, fullName);
        assignRealmRole(accessToken, userId, role);
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakBaseUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", adminClientId);
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), Map.class);
            Object token = response.getBody() != null ? response.getBody().get("access_token") : null;
            if (token == null) {
                throw new IllegalStateException("Failed to obtain Keycloak admin access token");
            }
            return token.toString();
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("Failed to obtain Keycloak admin access token: " + ex.getResponseBodyAsString(), ex);
        }
    }

    private String createUser(String accessToken, String username, String password, String fullName) {
        String usersUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users";

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("enabled", true);
        payload.put("email", username);
        payload.put("emailVerified", true);
        if (fullName != null && !fullName.isBlank()) {
            payload.put("firstName", fullName);
        }

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", password);
        credential.put("temporary", false);
        payload.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, authHeaders(accessToken));

        try {
            ResponseEntity<Void> response = restTemplate.exchange(usersUrl, HttpMethod.POST, request, Void.class);
            URI location = response.getHeaders().getLocation();
            if (location != null) {
                String path = location.getPath();
                return path.substring(path.lastIndexOf('/') + 1);
            }
            return findUserIdByUsername(accessToken, username);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new IllegalArgumentException("User already exists");
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("Failed to create Keycloak user: " + ex.getResponseBodyAsString(), ex);
        }
    }

    private String findUserIdByUsername(String accessToken, String username) {
        String usersLookupUrl = UriComponentsBuilder
                .fromHttpUrl(keycloakBaseUrl + "/admin/realms/" + realm + "/users")
                .queryParam("username", username)
                .toUriString();

        ResponseEntity<List> response = restTemplate.exchange(
                usersLookupUrl,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(accessToken)),
                List.class
        );

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new IllegalStateException("User was created but could not resolve user id from Keycloak");
        }

        for (Object item : response.getBody()) {
            if (item instanceof Map<?, ?> map) {
                Object foundUsername = map.get("username");
                if (foundUsername != null && username.equalsIgnoreCase(foundUsername.toString())) {
                    Object id = map.get("id");
                    if (id != null) {
                        return id.toString();
                    }
                }
            }
        }

        throw new IllegalStateException("User id not found in Keycloak response");
    }

    private void assignRealmRole(String accessToken, String userId, String role) {
        String normalizedRole = role == null || role.isBlank() ? "PATIENT" : role.toUpperCase();

        String roleUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/roles/" + normalizedRole;
        ResponseEntity<Map> roleResponse = restTemplate.exchange(
                roleUrl,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(accessToken)),
                Map.class
        );

        if (roleResponse.getBody() == null) {
            throw new IllegalStateException("Role not found in Keycloak: " + normalizedRole);
        }

        String mappingUrl = keycloakBaseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        HttpEntity<List<Map>> mappingRequest = new HttpEntity<>(List.of(roleResponse.getBody()), authHeaders(accessToken));

        restTemplate.exchange(mappingUrl, HttpMethod.POST, mappingRequest, Void.class);
    }

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
