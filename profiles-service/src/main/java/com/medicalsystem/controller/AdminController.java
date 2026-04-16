package com.medicalsystem.controller;

import com.medicalsystem.dto.AppointmentAdminDTO;
import com.medicalsystem.model.Patient;
import com.medicalsystem.model.Payment;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.PatientService;
import com.medicalsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.medicalsystem.model.Doctor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/doctors")
    public ResponseEntity<?> allDoctors(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/patients")
    public ResponseEntity<?> allPatients(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        return ResponseEntity.ok(patientService.getAllPatients());
    }

        @GetMapping("/appointments")
        public ResponseEntity<?> allAppointments(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        List<AppointmentAdminDTO> appointments = appointmentService.getAllAppointments().stream()
            .map(appointment -> {
                String patientName = patientService.getPatientById(appointment.getPatientId())
                    .map(Patient::getName)
                    .orElse("Unknown Patient");

                String doctorName = doctorService.getDoctorById(appointment.getDoctorId())
                    .map(Doctor::getName)
                    .orElse("Unknown Doctor");

                return new AppointmentAdminDTO(
                    String.valueOf(appointment.getId()),
                    patientName,
                    doctorName,
                    appointment.getAppointmentTime(),
                    appointment.getStatus(),
                    appointment.getReason(),
                    appointment.getUrgencyLevel()
                );
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
        }

    @GetMapping("/payments")
    public ResponseEntity<?> allPayments(Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id, Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok().body(new Message("SUCCESS", "Doctor deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Message("ERROR", "Failed to delete doctor: " + e.getMessage()));
        }
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id, Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok().body(new Message("SUCCESS", "Patient deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Message("ERROR", "Failed to delete patient: " + e.getMessage()));
        }
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, Authentication authentication) {
        if (!authorizationService.isAdmin(authentication)) {
            return forbiddenAdmin();
        }

        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok().body(new Message("SUCCESS", "Appointment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Message("ERROR", "Failed to delete appointment: " + e.getMessage()));
        }
    }

    private ResponseEntity<Message> forbiddenAdmin() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Message("ERROR", "Only admin can access this endpoint"));
    }

    public static class Message {
        private String type;
        private String content;

        public Message(String type, String content) {
            this.type = type;
            this.content = content;
        }
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
