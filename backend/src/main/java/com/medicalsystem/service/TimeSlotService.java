package com.medicalsystem.service;

import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeSlotService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    public TimeSlot createTimeSlot(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(sequenceGeneratorService.generateSequence("timeslot_seq"));
        timeSlot.setDoctorId(doctorId);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        // Newly created slots are available for booking
        timeSlot.setAvailable(true);

        return timeSlotRepository.save(timeSlot);
    }

    public List<TimeSlot> getTimeslotsByDoctorId(Long doctorId) {
        return timeSlotRepository.findByDoctorId(doctorId);
    }

    public List<TimeSlot> getAvailableTimeSlotsByDoctorId(Long doctorId) {
        return timeSlotRepository.findByDoctorIdAndIsAvailable(doctorId, true);
    }

    public Optional<TimeSlot> findById(Long slotId) {
        return timeSlotRepository.findById(slotId);
    }

    public TimeSlot bookTimeSlot(Long slotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + slotId));

        if (!timeSlot.isAvailable()) {
            throw new IllegalStateException("Time slot is already booked");
        }

        timeSlot.setAvailable(false);
        return timeSlotRepository.save(timeSlot);
    }

    public TimeSlot updateTimeSlot(TimeSlot timeSlot) {
        timeSlotRepository.findById(timeSlot.getId())
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + timeSlot.getId()));

        return timeSlotRepository.save(timeSlot);
    }

    public void deleteTimeSlot(Long slotId) {
        timeSlotRepository.deleteById(slotId);
    }
}