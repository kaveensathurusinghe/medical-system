package com.medicalsystem.appointments.service;

import com.medicalsystem.appointments.dto.AppointmentDTO;
import com.medicalsystem.appointments.model.Appointment;
import com.medicalsystem.appointments.model.TimeSlot;
import com.medicalsystem.appointments.repository.AppointmentRepository;
import com.medicalsystem.appointments.repository.DoctorRepository;
import com.medicalsystem.appointments.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private TimeSlotService timeSlotService;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    public AppointmentService(AppointmentRepository appointmentRepository, TimeSlotService timeSlotService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotService = timeSlotService;
    }

    public Appointment createAppointment(Long patientId, Long doctorId,
                                         Long slotId, int urgencyLevel, String reason) {
        TimeSlot timeSlot = timeSlotService.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        if (!timeSlot.isAvailable()) {
            throw new RuntimeException("Time slot is already booked");
        }

        if (!timeSlot.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Time slot doesn't belong to this doctor");
        }

        Appointment appointment = new Appointment();
        appointment.setId(sequenceGeneratorService.generateSequence("appointment_seq"));
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentTime(timeSlot.getStartTime());
        appointment.setStatus("SCHEDULED");
        appointment.setUrgencyLevel(urgencyLevel);
        appointment.setReason(reason);
        timeSlotService.bookTimeSlot(slotId);

        try {
            return appointmentRepository.save(appointment);
        } catch (RuntimeException e) {
            timeSlotService.releaseTimeSlot(slotId);
            throw e;
        }
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Appointment updateAppointment(Appointment updatedAppointment) {
        Appointment existing = appointmentRepository.findById(updatedAppointment.getId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (existing.getStatus().equals("COMPLETED") || existing.getStatus().equals("CANCELLED")) {
            if (!updatedAppointment.getStatus().equals(existing.getStatus())) {
                throw new IllegalStateException("Cannot change status from " + existing.getStatus());
            }
        }

        if (updatedAppointment.getUrgencyLevel() < 1 || updatedAppointment.getUrgencyLevel() > 5) {
            throw new IllegalArgumentException("Urgency level must be between 1 and 5");
        }

        return appointmentRepository.save(updatedAppointment);
    }

    public Appointment saveAppointment(Appointment appointment) {
        if (appointment.getId() == null) {
            appointment.setId(sequenceGeneratorService.generateSequence("appointment_seq"));
        }
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    public void rollbackCreatedAppointment(Long appointmentId, Long slotId) {
        if (appointmentId != null) {
            appointmentRepository.deleteById(appointmentId);
        }
        if (slotId != null) {
            timeSlotService.releaseTimeSlot(slotId);
        }
    }

    public Appointment rescheduleAppointment(Long id, LocalDateTime newTime) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setAppointmentTime(newTime);
            return saveAppointment(appointment);
        }
        return null;
    }

    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AppointmentDTO convertToDto(Appointment appointment) {
        String patientName = patientRepository.findById(appointment.getPatientId())
                .map(p -> p.getName())
                .orElse("Unknown Patient");

        String doctorName = doctorRepository.findById(appointment.getDoctorId())
                .map(d -> d.getName())
                .orElse("Unknown Doctor");

        return new AppointmentDTO(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                patientName,
                doctorName,
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getReason()
        );
    }

    public long getCompletedAppointmentsCount() {
        return appointmentRepository.findAll().stream()
                .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                .count();
    }

    public long getTodayAppointmentsCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return appointmentRepository.findAll().stream()
            .filter(a -> a.getAppointmentTime() != null)
            .filter(a -> !a.getAppointmentTime().isBefore(startOfDay)
                && !a.getAppointmentTime().isAfter(endOfDay))
            .count();
    }

    public List<AppointmentDTO> getRecentAppointments(int limit) {
        return appointmentRepository
                .findAll(PageRequest.of(0, limit, Sort.by("appointmentTime").descending()))
                .getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getUpcomingAppointmentsCountForDoctor(Long doctorId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(a -> a.getAppointmentTime().isAfter(now))
                .count();
    }

    public long getTodayAppointmentsCountForDoctor(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(a -> !a.getAppointmentTime().isBefore(startOfDay)
                        && !a.getAppointmentTime().isAfter(endOfDay))
                .count();
    }

    public long getUpcomingAppointmentsCountForPatient(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByPatientId(patientId).stream()
                .filter(a -> a.getAppointmentTime().isAfter(now))
                .count();
    }

    public long getTotalAppointmentsCountForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).size();
    }
}
