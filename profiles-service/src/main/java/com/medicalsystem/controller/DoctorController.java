package com.medicalsystem.controller;

import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.dto.DoctorDashboardStats;
import com.medicalsystem.model.Doctor;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id,
                                          @RequestBody Doctor doctorDetails,
                                          Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only update your own doctor profile"));
        }

        Doctor doctor = doctorService.getDoctorById(id).orElse(null);
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            doctor.setName(doctorDetails.getName());
            doctor.setSpecialization(doctorDetails.getSpecialization());
            doctor.setEmail(doctorDetails.getEmail());
            doctor.setPhone(doctorDetails.getPhone());
            if (doctorDetails.getConsultationFee() != null) {
                doctor.setConsultationFee(doctorDetails.getConsultationFee());
            }
            return ResponseEntity.ok(doctorService.saveDoctor(doctor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @GetMapping("/{id}/dashboard-stats")
    public ResponseEntity<?> getDashboardStats(@PathVariable Long id,
                                               Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only view your own dashboard"));
        }

        DoctorDashboardStats stats = doctorService.getDashboardStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable Long id,
                                                   @RequestParam(required = false) Integer limit,
                                                   Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only view your own appointments"));
        }

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(id);
        if (limit != null && limit > 0 && limit < appointments.size()) {
            appointments = appointments.subList(0, limit);
        }
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Doctor account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", "Failed to delete doctor: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                            @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only change your own password"));
        }

        try {
            doctorService.updatePassword(id, request.getNewPassword());
            return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", "Failed to change password"));
        }
    }

    public static class ChangePasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

