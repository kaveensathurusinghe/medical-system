package com.medicalsystem.service;

import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.TimeSlot;
import com.medicalsystem.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.medicalsystem.model.Patient;
import com.medicalsystem.model.Doctor;

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
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;

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
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentTime(timeSlot.getStartTime());
        appointment.setStatus("SCHEDULED");
        appointment.setUrgencyLevel(urgencyLevel);
        appointment.setReason(reason);
        timeSlotService.bookTimeSlot(slotId);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Appointment updateAppointment(Appointment updatedAppointment) {
        // Get existing appointment
        Appointment existing = appointmentRepository.findById(updatedAppointment.getId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Validate status transition
        if (existing.getStatus().equals("COMPLETED") || existing.getStatus().equals("CANCELLED")) {
            if (!updatedAppointment.getStatus().equals(existing.getStatus())) {
                throw new IllegalStateException("Cannot change status from " + existing.getStatus());
            }
        }

        // Validate urgency level
        if (updatedAppointment.getUrgencyLevel() < 1 || updatedAppointment.getUrgencyLevel() > 5) {
            throw new IllegalArgumentException("Urgency level must be between 1 and 5");
        }

        return appointmentRepository.save(updatedAppointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    public Appointment rescheduleAppointment(Long id, LocalDateTime newTime) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setAppointmentTime(newTime);
            return appointmentRepository.save(appointment);
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
        Patient patient = patientService.getPatientById(appointment.getPatientId()).orElse(null);
        Doctor doctor = doctorService.getDoctorById(appointment.getDoctorId()).orElse(null);
        String patientName = patient != null ? patient.getName() : "Unknown Patient";
        String doctorName = doctor != null ? doctor.getName() : "Unknown Doctor";
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

    /**
     * Count all completed appointments (for admin dashboards).
     */
    public long getCompletedAppointmentsCount() {
        return appointmentRepository.findAll().stream()
                .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                .count();
    }

    /**
     * Return the most recent appointments ordered by appointmentTime desc.
     */
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