package com.medicalsystem.controller;

import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.dto.BookAppointmentRequest;
import com.medicalsystem.model.Appointment;
import com.medicalsystem.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentRequest request) {
        try {
            Long patientId = Long.valueOf(request.getPatientId());
            Long doctorId = Long.valueOf(request.getDoctorId());
            Long slotId = Long.valueOf(request.getSlotId());

            Appointment appointment = appointmentService.createAppointment(
                    patientId,
                    doctorId,
                    slotId,
                    request.getUrgencyLevel(),
                    request.getReason()
            );

            AppointmentDTO dto = appointmentService.convertToDto(appointment);
            return ResponseEntity.ok(dto);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new AdminController.Message("ERROR", "Invalid ID format for patient/doctor/slot"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id)
                .map(a -> ResponseEntity.ok(appointmentService.convertToDto(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@RequestBody RescheduleRequest request) {
        try {
            LocalDateTime newTime = LocalDateTime.parse(request.getNewAppointmentTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Appointment appointment = appointmentService.rescheduleAppointment(request.getId(), newTime);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Appointment rescheduled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(@PathVariable Long patientId,
                                                                         @RequestParam(value = "limit", required = false) Integer limit) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        if (limit != null && limit < appointments.size()) {
            appointments = appointments.subList(0, limit);
        }
        return ResponseEntity.ok(appointments);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Appointment deleted successfully"));
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
}