package com.medicalsystem.appointments.controller;

import com.medicalsystem.appointments.dto.AppointmentDTO;
import com.medicalsystem.appointments.dto.BookAppointmentRequest;
import com.medicalsystem.appointments.model.Appointment;
import com.medicalsystem.appointments.service.AppointmentService;
import com.medicalsystem.appointments.service.AuthorizationService;
import com.medicalsystem.appointments.repository.DoctorRepository;
import com.medicalsystem.appointments.repository.PatientRepository;
import com.medicalsystem.appointments.service.PaymentService;
import com.medicalsystem.appointments.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/booking-fee")
    public ResponseEntity<?> getBookingFee(@RequestParam(value = "doctorId", required = false) Long doctorId) {
        double defaultFee = systemConfigService.getConfig().getAppointmentFee();
        Double doctorFee = doctorId == null
                ? null
                : doctorRepository.findById(doctorId)
                    .map(d -> d.getConsultationFee())
                    .orElse(null);

        boolean useDoctorFee = doctorFee != null && doctorFee > 0;
        Map<String, Object> response = new HashMap<>();
        response.put("appointmentFee", useDoctorFee ? doctorFee : defaultFee);
        response.put("feeSource", useDoctorFee ? "doctor" : "default");
        response.put("doctorId", doctorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentRequest request,
                                             Authentication authentication) {
        try {
            Long patientId;
            boolean authenticatedPatient = authentication != null
                    && authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_PATIENT".equals(a.getAuthority()));

            if (authenticatedPatient) {
                patientId = patientRepository.findByEmail(authentication.getName())
                        .map(p -> p.getId())
                        .orElseThrow(() -> new RuntimeException("Authenticated patient not found"));
            } else {
                patientId = Long.valueOf(request.getPatientId());
            }

            Long doctorId = Long.valueOf(request.getDoctorId());
            Long slotId = Long.valueOf(request.getSlotId());

            double appointmentFee = resolveAppointmentFee(doctorId);
            if (appointmentFee <= 0) {
                throw new RuntimeException("Appointment fee is not configured");
            }

            Appointment appointment = appointmentService.createAppointment(
                    patientId,
                    doctorId,
                    slotId,
                    request.getUrgencyLevel(),
                    request.getReason()
            );

            paymentService.processPayment(
                    String.valueOf(appointment.getId()),
                    String.valueOf(patientId),
                    String.valueOf(doctorId),
                    appointmentFee,
                    request.getPaymentMethod(),
                    request.getCardNumber(),
                    request.getCardHolderName(),
                    request.getExpiryDate(),
                    request.getCvv()
            );

            AppointmentDTO dto = appointmentService.convertToDto(appointment);
            return ResponseEntity.ok(dto);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiMessage("ERROR", "Invalid ID format for patient/doctor/slot"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    private double resolveAppointmentFee(Long doctorId) {
        double defaultFee = systemConfigService.getConfig().getAppointmentFee();
        if (doctorId == null) {
            return defaultFee;
        }

        return doctorRepository.findById(doctorId)
                .map(doctor -> {
                    Double fee = doctor.getConsultationFee();
                    if (fee != null && fee > 0) {
                        return fee;
                    }
                    return defaultFee;
                })
                .orElse(defaultFee);
    }

    @GetMapping
    public ResponseEntity<?> getAllAppointments(@RequestParam(value = "limit", required = false) Integer limit,
                                                Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "Only admins can view all appointments"));
        }

        List<AppointmentDTO> appointments = appointmentService.getAllAppointments().stream()
                .map(appointmentService::convertToDto)
                .toList();

        if (limit != null && limit > 0 && limit < appointments.size()) {
            appointments = appointments.subList(0, limit);
        }
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsByDoctor(@PathVariable Long doctorId,
                                                     @RequestParam(value = "limit", required = false) Integer limit,
                                                     Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You can only view your own appointments"));
        }

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        if (limit != null && limit > 0 && limit < appointments.size()) {
            appointments = appointments.subList(0, limit);
        }
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id,
                                                Authentication authentication) {
        Appointment appointment = appointmentService.getAppointmentById(id).orElse(null);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }

        if (!authorizationService.canAccessAppointment(authentication, appointment)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You are not allowed to view this appointment"));
        }

        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }

    @PutMapping("/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@RequestBody RescheduleRequest request,
                                                   Authentication authentication) {
        try {
            Appointment existing = appointmentService.getAppointmentById(request.getId()).orElse(null);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            if (!authorizationService.canAccessAppointment(authentication, existing)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiMessage("ERROR", "You are not allowed to reschedule this appointment"));
            }

            LocalDateTime newTime = LocalDateTime.parse(request.getNewAppointmentTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Appointment appointment = appointmentService.rescheduleAppointment(request.getId(), newTime);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new ApiMessage("SUCCESS", "Appointment rescheduled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateAppointment(@RequestBody AppointmentDTO request,
                                               Authentication authentication) {
        if (request.getAppointmentId() == null) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", "Appointment id is required"));
        }

        Appointment existing = appointmentService.getAppointmentById(request.getAppointmentId()).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (!authorizationService.canAccessAppointment(authentication, existing)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You are not allowed to update this appointment"));
        }

        if (request.getAppointmentTime() != null) {
            existing.setAppointmentTime(request.getAppointmentTime());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getReason() != null) {
            existing.setReason(request.getReason());
        }

        try {
            Appointment updated = appointmentService.updateAppointment(existing);
            return ResponseEntity.ok(appointmentService.convertToDto(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAppointmentsByPatient(@PathVariable Long patientId,
                                                      @RequestParam(value = "limit", required = false) Integer limit,
                                                      Authentication authentication) {
        if (!authorizationService.canAccessPatient(authentication, patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You can only view your own appointments"));
        }

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        if (limit != null && limit < appointments.size()) {
            appointments = appointments.subList(0, limit);
        }
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new ApiMessage("SUCCESS", "Appointment deleted successfully"));
    }

    public static class RescheduleRequest {
        private Long id;
        private String newAppointmentTime;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNewAppointmentTime() {
            return newAppointmentTime;
        }

        public void setNewAppointmentTime(String newAppointmentTime) {
            this.newAppointmentTime = newAppointmentTime;
        }
    }

    // small ApiMessage used locally
    public static class ApiMessage {
        private String type;
        private String content;

        public ApiMessage() {}

        public ApiMessage(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
