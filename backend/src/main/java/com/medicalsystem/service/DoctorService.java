package com.medicalsystem.service;

import com.medicalsystem.dto.DoctorDashboardStats;
import com.medicalsystem.model.Appointment;
import com.medicalsystem.model.Doctor;
import com.medicalsystem.repository.AppointmentRepository;
import com.medicalsystem.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Doctor registerDoctor(String name, String email, String rawPassword,
                                 String phone, String specialization) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPhone(phone);
        doctor.setSpecialization(specialization);
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

        long upcoming = appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(a -> a.getAppointmentTime().isAfter(LocalDateTime.now()))
                .count();

        long todayCount = appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(a -> !a.getAppointmentTime().isBefore(startOfDay)
                        && !a.getAppointmentTime().isAfter(endOfDay))
                .count();

        long totalPatients = appointmentRepository.findByDoctorId(doctorId).stream()
                .map(Appointment::getPatientId)
                .distinct()
                .count();

        DoctorDashboardStats stats = new DoctorDashboardStats();
        stats.setUpcomingAppointments(upcoming);
        stats.setTodayAppointments(todayCount);
        stats.setTotalPatients(totalPatients);
        return stats;
    }
}
