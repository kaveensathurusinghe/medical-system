package com.medicalsystem.controller;

import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.dto.DoctorDashboardStats;
import com.medicalsystem.model.Doctor;
import com.medicalsystem.service.AppointmentService;
import com.medicalsystem.service.DoctorService;
import com.medicalsystem.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @PostMapping("/register")
    public ResponseEntity<Doctor> registerDoctor(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.saveDoctor(doctor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        doctor.setName(doctorDetails.getName());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setPhone(doctorDetails.getPhone());

        return ResponseEntity.ok(doctorService.saveDoctor(doctor));
    }

    @GetMapping("/{id}/dashboard-stats")
    public ResponseEntity<DoctorDashboardStats> getDashboardStats(@PathVariable Long id) {
        DoctorDashboardStats stats = doctorService.getDashboardStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long id,
                                                                      @RequestParam(required = false) Integer limit) {
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
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
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

