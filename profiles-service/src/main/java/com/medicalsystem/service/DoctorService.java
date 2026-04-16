package com.medicalsystem.service;

import com.medicalsystem.dto.DoctorDashboardStats;
import com.medicalsystem.dto.AppointmentDTO;
import com.medicalsystem.model.Doctor;
import com.medicalsystem.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoctorCategoryService doctorCategoryService;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private PaymentService paymentService;

    public Doctor registerDoctor(String name, String email, String rawPassword,
                                 String phone, String specialization) {
        Doctor doctor = new Doctor();
        doctor.setId(sequenceGeneratorService.generateSequence("doctor_seq"));
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setSpecialization(doctorCategoryService.resolveValidCategoryName(specialization));
        doctor.setPassword(passwordEncoder.encode(rawPassword));
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> findByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor saveDoctor(Doctor doctor) {
        if (doctor.getId() == null) {
            doctor.setId(sequenceGeneratorService.generateSequence("doctor_seq"));
        }

        if (doctor.getConsultationFee() != null && doctor.getConsultationFee() <= 0) {
            throw new IllegalArgumentException("Consultation fee must be greater than zero");
        }

        doctor.setSpecialization(doctorCategoryService.resolveValidCategoryName(doctor.getSpecialization()));
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public long totalDoctors() {
        return doctorRepository.count();
    }

    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    public void updatePassword(Long id, String newPassword) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setPassword(passwordEncoder.encode(newPassword));
        doctorRepository.save(doctor);
    }

    /**
     * Aggregate basic dashboard statistics for a doctor.
     */
    public DoctorDashboardStats getDashboardStats(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);

        long upcoming = appointments.stream()
            .filter(a -> a.getAppointmentTime() != null && a.getAppointmentTime().isAfter(LocalDateTime.now()))
            .count();

        long todayCount = appointments.stream()
            .filter(a -> a.getAppointmentTime() != null &&
                !a.getAppointmentTime().isBefore(startOfDay) && !a.getAppointmentTime().isAfter(endOfDay))
            .count();

        long totalPatients = appointments.stream()
            .map(AppointmentDTO::getPatientId)
            .filter(Objects::nonNull)
            .distinct()
            .count();

        double totalIncome = paymentService.getTotalIncomeByDoctorId(String.valueOf(doctorId));

        DoctorDashboardStats stats = new DoctorDashboardStats();
        stats.setUpcomingAppointments(upcoming);
        stats.setTodayAppointments(todayCount);
        stats.setTotalPatients(totalPatients);
        stats.setTotalIncome(totalIncome);
        return stats;
    }
}
