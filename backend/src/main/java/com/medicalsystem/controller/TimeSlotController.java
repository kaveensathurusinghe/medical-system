package com.medicalsystem.controller;

import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.service.AuthorizationService;
import com.medicalsystem.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<List<TimeSlot>> getAvailableTimeSlots(@PathVariable Long doctorId) {
        return ResponseEntity.ok(timeSlotService.getAvailableTimeSlotsByDoctorId(doctorId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getTimeslotsByDoctorId(@PathVariable Long doctorId,
                                                    Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only view your own timeslots"));
        }

        return ResponseEntity.ok(timeSlotService.getTimeslotsByDoctorId(doctorId));
    }

    @PostMapping("/doctor/{doctorId}/generate")
    public ResponseEntity<?> generateTimeSlots(@PathVariable Long doctorId,
                                               @RequestBody TimeSlotRequest request,
                                               Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AdminController.Message("ERROR", "You can only manage your own timeslots"));
        }

        try {
            LocalDateTime availableFrom = LocalDateTime.parse(request.getStartDateTime());
            LocalDateTime availableTo = LocalDateTime.parse(request.getEndDateTime());
            int slotDurationMinutes = request.getSlotDurationMinutes();

            List<TimeSlot> generatedSlots = timeSlotService.generateTimeSlots(
                    doctorId,
                    availableFrom,
                    availableTo,
                    slotDurationMinutes
            );
            return ResponseEntity.ok(generatedSlots);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(new AdminController.Message("ERROR", "Invalid datetime format. Use ISO format yyyy-MM-ddTHH:mm:ss"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteTimeSlot(@PathVariable Long slotId,
                                            Authentication authentication) {
        try {
            TimeSlot slot = timeSlotService.findById(slotId)
                    .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + slotId));

            if (!authorizationService.canAccessDoctor(authentication, slot.getDoctorId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AdminController.Message("ERROR", "You can only delete your own timeslots"));
            }

            timeSlotService.deleteTimeSlot(slotId);
            return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Time slot deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    public static class TimeSlotRequest {
        private String startDateTime;
        private String endDateTime;
        private int slotDurationMinutes;

        public String getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(String startDateTime) {
            this.startDateTime = startDateTime;
        }

        public String getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(String endDateTime) {
            this.endDateTime = endDateTime;
        }

        public int getSlotDurationMinutes() {
            return slotDurationMinutes;
        }

        public void setSlotDurationMinutes(int slotDurationMinutes) {
            this.slotDurationMinutes = slotDurationMinutes;
        }
    }
}