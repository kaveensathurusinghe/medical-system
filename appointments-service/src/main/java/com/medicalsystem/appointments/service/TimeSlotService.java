package com.medicalsystem.appointments.service;

import com.medicalsystem.appointments.model.TimeSlot;
import com.medicalsystem.appointments.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return timeSlotRepository.findByDoctorIdOrderByStartTimeAsc(doctorId);
    }

    public List<TimeSlot> getAvailableTimeSlotsByDoctorId(Long doctorId) {
        return timeSlotRepository.findByDoctorIdAndIsAvailableOrderByStartTimeAsc(doctorId, true);
    }

    public List<TimeSlot> generateTimeSlots(Long doctorId,
                                            LocalDateTime availableFrom,
                                            LocalDateTime availableTo,
                                            int slotDurationMinutes) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor ID is required");
        }
        if (availableFrom == null || availableTo == null) {
            throw new IllegalArgumentException("Available from/to time is required");
        }
        if (!availableTo.isAfter(availableFrom)) {
            throw new IllegalArgumentException("Available end time must be after start time");
        }
        if (slotDurationMinutes <= 0 || slotDurationMinutes > 180) {
            throw new IllegalArgumentException("Slot duration must be between 1 and 180 minutes");
        }

        long totalMinutes = Duration.between(availableFrom, availableTo).toMinutes();
        if (totalMinutes < slotDurationMinutes) {
            throw new IllegalArgumentException("Availability range is shorter than slot duration");
        }

        List<TimeSlot> newSlots = new ArrayList<>();
        LocalDateTime slotStart = availableFrom;

        while (!slotStart.plusMinutes(slotDurationMinutes).isAfter(availableTo)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);

            boolean overlapsExisting = !timeSlotRepository
                    .findByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(doctorId, slotEnd, slotStart)
                    .isEmpty();

            if (!overlapsExisting) {
                TimeSlot slot = new TimeSlot();
                slot.setId(sequenceGeneratorService.generateSequence("timeslot_seq"));
                slot.setDoctorId(doctorId);
                slot.setStartTime(slotStart);
                slot.setEndTime(slotEnd);
                slot.setAvailable(true);
                newSlots.add(slot);
            }

            slotStart = slotEnd;
        }

        if (newSlots.isEmpty()) {
            throw new IllegalStateException("No new slots generated. Selected range overlaps existing slots.");
        }

        return timeSlotRepository.saveAll(newSlots);
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

    public TimeSlot releaseTimeSlot(Long slotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + slotId));

        timeSlot.setAvailable(true);
        return timeSlotRepository.save(timeSlot);
    }

    public TimeSlot updateTimeSlot(TimeSlot timeSlot) {
        timeSlotRepository.findById(timeSlot.getId())
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + timeSlot.getId()));

        return timeSlotRepository.save(timeSlot);
    }

    public void deleteTimeSlot(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with ID: " + slotId));

        if (!slot.isAvailable()) {
            throw new IllegalStateException("Booked slots cannot be deleted");
        }

        timeSlotRepository.deleteById(slotId);
    }
}
