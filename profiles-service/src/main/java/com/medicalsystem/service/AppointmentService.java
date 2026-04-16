package com.medicalsystem.service;

import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.model.Appointment;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final RestTemplate restTemplate;

    @Value("${appointments.service.base-url:http://appointments-service:8083/api}")
    private String appointmentsBaseUrl;

    @Autowired
    @Lazy
    private DoctorService doctorService;
    @Autowired
    @Lazy
    private PatientService patientService;

    public AppointmentService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Appointment createAppointment(Long patientId, Long doctorId,
                                         Long slotId, int urgencyLevel, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("patientId", String.valueOf(patientId));
        payload.put("doctorId", String.valueOf(doctorId));
        payload.put("slotId", String.valueOf(slotId));
        payload.put("urgencyLevel", urgencyLevel);
        payload.put("reason", reason);

        String url = appointmentsBaseUrl + "/appointments/book";
        ResponseEntity<AppointmentDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                authEntity(payload),
                AppointmentDTO.class
        );
        return dtoToModel(response.getBody());
    }

    public List<Appointment> getAllAppointments() {
        try {
            String url = appointmentsBaseUrl + "/appointments";
            ResponseEntity<List<AppointmentDTO>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<List<AppointmentDTO>>() {}
            );
            List<AppointmentDTO> dtos = resp.getBody();
            if (dtos == null) return Collections.emptyList();
            List<Appointment> out = new ArrayList<>();
            for (AppointmentDTO dto : dtos) out.add(dtoToModel(dto));
            return out;
        } catch (RestClientResponseException e) {
            return Collections.emptyList();
        }
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        try {
            String url = appointmentsBaseUrl + "/appointments/" + id;
            ResponseEntity<AppointmentDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    AppointmentDTO.class
            );
            AppointmentDTO dto = response.getBody();
            return Optional.ofNullable(dtoToModel(dto));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Appointment updateAppointment(Appointment updatedAppointment) {
        String url = appointmentsBaseUrl + "/appointments";
        AppointmentDTO dto = modelToDto(updatedAppointment);
        HttpEntity<AppointmentDTO> req = authEntity(dto);
        ResponseEntity<AppointmentDTO> resp = restTemplate.exchange(url, HttpMethod.PUT, req, AppointmentDTO.class);
        return dtoToModel(resp.getBody());
    }

    public Appointment saveAppointment(Appointment appointment) {
        return updateAppointment(appointment);
    }

    public void deleteAppointment(Long id) {
        String url = appointmentsBaseUrl + "/appointments/" + id;
        restTemplate.exchange(url, HttpMethod.DELETE, authEntity(), Void.class);
    }

    public void rollbackCreatedAppointment(Long appointmentId, Long slotId) {
        if (appointmentId != null) {
            deleteAppointment(appointmentId);
        }
        if (slotId != null) {
            // release timeslot in appointments service
            String url = appointmentsBaseUrl + "/timeslots/" + slotId + "/release";
            restTemplate.exchange(url, HttpMethod.POST, authEntity(), Void.class);
        }
    }

    public Appointment rescheduleAppointment(Long id, LocalDateTime newTime) {
        Map<String, Object> req = new HashMap<>();
        req.put("id", id);
        req.put("newAppointmentTime", newTime.toString());
        String url = appointmentsBaseUrl + "/appointments/reschedule";
        ResponseEntity<AppointmentDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                authEntity(req),
                AppointmentDTO.class
        );
        AppointmentDTO dto = response.getBody();
        return dtoToModel(dto);
    }

    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) {
        try {
            String url = appointmentsBaseUrl + "/appointments/patient/" + patientId;
            ResponseEntity<List<AppointmentDTO>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<List<AppointmentDTO>>() {}
            );
            return resp.getBody() == null ? Collections.emptyList() : resp.getBody();
        } catch (RestClientResponseException e) {
            return Collections.emptyList();
        }
    }

    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {
        try {
            String url = appointmentsBaseUrl + "/appointments/doctor/" + doctorId;
            ResponseEntity<List<AppointmentDTO>> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authEntity(),
                    new ParameterizedTypeReference<List<AppointmentDTO>>() {}
            );
            return resp.getBody() == null ? Collections.emptyList() : resp.getBody();
        } catch (RestClientResponseException e) {
            return Collections.emptyList();
        }
    }

    public long getCompletedAppointmentsCount() {
        return getAllAppointments().stream()
                .filter(a -> a.getStatus() != null && a.getStatus().equalsIgnoreCase("COMPLETED"))
                .count();
    }

    public List<AppointmentDTO> getRecentAppointments(int n) {
        return getAllAppointments().stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(n)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getUpcomingAppointmentsCountForPatient(Long patientId) {
        return getAppointmentsByPatientId(patientId).stream()
                .filter(a -> a.getAppointmentTime() != null && a.getAppointmentTime().isAfter(LocalDateTime.now()))
                .count();
    }

    public long getTotalAppointmentsCountForPatient(Long patientId) {
        return getAppointmentsByPatientId(patientId).size();
    }

    public long getTodayAppointmentsCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return getAllAppointments().stream()
                .filter(a -> a.getAppointmentTime() != null &&
                        !a.getAppointmentTime().isBefore(startOfDay) && !a.getAppointmentTime().isAfter(endOfDay))
                .count();
    }

    public AppointmentDTO convertToDto(Appointment appointment) {
        String patientName = patientService.getPatientById(appointment.getPatientId())
                .map(p -> p.getName())
                .orElse("Unknown Patient");

        String doctorName = doctorService.getDoctorById(appointment.getDoctorId())
                .map(d -> d.getName())
                .orElse("Unknown Doctor");

        AppointmentDTO dto = new AppointmentDTO();
        dto.setAppointmentId(appointment.getId());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setPatientName(patientName);
        dto.setDoctorName(doctorName);
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setReason(appointment.getReason());
        return dto;
    }

    private Appointment dtoToModel(AppointmentDTO dto) {
        if (dto == null) return null;
        Appointment a = new Appointment();
        a.setId(dto.getAppointmentId());
        a.setPatientId(dto.getPatientId());
        a.setDoctorId(dto.getDoctorId());
        a.setAppointmentTime(dto.getAppointmentTime());
        a.setStatus(dto.getStatus());
        a.setReason(dto.getReason());
        return a;
    }

    private AppointmentDTO modelToDto(Appointment appointment) {
        return convertToDto(appointment);
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