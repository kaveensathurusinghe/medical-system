package com.medicalsystem.appointments.controller;

import com.medicalsystem.appointments.model.TimeSlot;
import com.medicalsystem.appointments.service.AuthorizationService;
import com.medicalsystem.appointments.service.TimeSlotService;
import com.medicalsystem.appointments.dto.ApiMessage;
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
                    .body(new ApiMessage("ERROR", "You can only view your own timeslots"));
        }

        return ResponseEntity.ok(timeSlotService.getTimeslotsByDoctorId(doctorId));
    }

    @PostMapping("/doctor/{doctorId}/generate")
    public ResponseEntity<?> generateTimeSlots(@PathVariable Long doctorId,
                                               @RequestBody TimeSlotRequest request,
                                               Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You can only manage your own timeslots"));
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
                    .body(new ApiMessage("ERROR", "Invalid datetime format. Use ISO format yyyy-MM-ddTHH:mm:ss"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/doctor/{doctorId}")
    public ResponseEntity<?> createTimeSlot(@PathVariable Long doctorId,
                                            @RequestBody SingleSlotRequest request,
                                            Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You can only manage your own timeslots"));
        }

        try {
            TimeSlot slot = timeSlotService.createTimeSlot(doctorId,
                    LocalDateTime.parse(request.getStartDateTime()),
                    LocalDateTime.parse(request.getEndDateTime()));
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<?> getSlotById(@PathVariable Long slotId) {
        return timeSlotService.findById(slotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{slotId}/book")
    public ResponseEntity<?> bookSlot(@PathVariable Long slotId) {
        try {
            TimeSlot booked = timeSlotService.bookTimeSlot(slotId);
            return ResponseEntity.ok(booked);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    @PostMapping("/{slotId}/release")
    public ResponseEntity<?> releaseSlot(@PathVariable Long slotId) {
        try {
            TimeSlot released = timeSlotService.releaseTimeSlot(slotId);
            return ResponseEntity.ok(released);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", e.getMessage()));
        }
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<?> updateSlot(@PathVariable Long slotId, @RequestBody TimeSlot slot, Authentication authentication) {
        if (!authorizationService.canAccessDoctor(authentication, slot.getDoctorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiMessage("ERROR", "You can only update your own timeslots"));
        }

        try {
            TimeSlot updated = timeSlotService.updateTimeSlot(slot);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", e.getMessage()));
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
                        .body(new ApiMessage("ERROR", "You can only delete your own timeslots"));
            }

            timeSlotService.deleteTimeSlot(slotId);
            return ResponseEntity.ok(new ApiMessage("SUCCESS", "Time slot deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiMessage("ERROR", e.getMessage()));
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

    public static class SingleSlotRequest {
        private String startDateTime;
        private String endDateTime;

        public String getStartDateTime() { return startDateTime; }
        public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }
        public String getEndDateTime() { return endDateTime; }
        public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }
    }
}
