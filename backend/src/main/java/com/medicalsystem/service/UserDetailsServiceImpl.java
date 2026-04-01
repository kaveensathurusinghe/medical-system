package com.medicalsystem.service;

import com.medicalsystem.model.Admin;
import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.Patient;
import com.medicalsystem.repository.AdminRepository;
import com.medicalsystem.repository.DoctorRepository;
import com.medicalsystem.repository.PatientRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public UserDetailsServiceImpl(AdminRepository adminRepository,
                                  PatientRepository patientRepository,
                                  DoctorRepository doctorRepository) {
        this.adminRepository = adminRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return User.builder()
                    .username(admin.get().getEmail())
                    .password(admin.get().getPassword())
                    .roles("ADMIN")
                    .build();
        }

        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) {
            return User.builder()
                    .username(patient.get().getEmail())
                    .password(patient.get().getPassword())
                    .roles("PATIENT")
                    .build();
        }

        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isPresent()) {
            return User.builder()
                    .username(doctor.get().getEmail())
                    .password(doctor.get().getPassword())
                    .roles("DOCTOR")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}