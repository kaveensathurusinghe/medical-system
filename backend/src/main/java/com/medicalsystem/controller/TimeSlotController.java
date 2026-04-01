package com.medicalsystem.controller;

import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<List<TimeSlot>> getAvailableTimeSlots(@PathVariable Long doctorId) {
        return ResponseEntity.ok(timeSlotService.getAvailableTimeSlotsByDoctorId(doctorId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<TimeSlot>> getTimeslotsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(timeSlotService.getTimeslotsByDoctorId(doctorId));
    }

    public static class TimeSlotRequest {
        private String startTime;
        private String endTime;

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
}